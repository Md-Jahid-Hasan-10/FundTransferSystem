package bank.service;

import bank.core.Bank;
import bank.exception.BankException;
import bank.exception.InsufficientFundsException;
import bank.exception.InvalidAmountException;
import bank.exception.InvalidOtpException;
import bank.exception.InvalidTransferException;
import bank.exception.OtpExpiredException;
import bank.model.Account;
import bank.model.AccountRole;
import bank.model.Customer;
import bank.model.RequestStatus;
import bank.model.Transaction;
import bank.model.TransactionRequest;
import bank.model.TransactionStatus;
import bank.model.TransactionType;
import bank.policy.TransferFeePolicy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Main entry point for financial operations.
 * Flow for withdrawals and transfers:
 * request -> verifyOtp -> confirm (or cancel at any point before confirm).
 */
public class FundTransferService {
    private static final BigDecimal ZERO = new BigDecimal("0.00");

    private final Bank bank;
    private final TransferFeePolicy feePolicy;           // transfers to other customers
    private final TransferFeePolicy ownAccountFeePolicy;   // transfers between the same customer's accounts
    private final OtpService otpService;
    private final NotificationService notificationService;
    private final Clock clock;
    private final long otpExpirySeconds;
    private final long requestExpirySeconds;

    public FundTransferService(Bank bank, TransferFeePolicy feePolicy, TransferFeePolicy ownAccountFeePolicy,
                               OtpService otpService, NotificationService notificationService, Clock clock,
                               long otpExpirySeconds, long requestExpirySeconds) {
        this.bank = bank;
        this.feePolicy = feePolicy;
        this.ownAccountFeePolicy = ownAccountFeePolicy;
        this.otpService = otpService;
        this.notificationService = notificationService;
        this.clock = clock;
        this.otpExpirySeconds = otpExpirySeconds;
        this.requestExpirySeconds = requestExpirySeconds;
    }

    // ---------- deposit (no OTP needed) ----------
    public Transaction deposit(Customer customer, String accountNumber, BigDecimal amount) {
        BigDecimal value = normalizeAmount(amount);
        Account account = findOwnedAccount(customer, accountNumber);
        account.deposit(value);
        Transaction tx = new Transaction(bank.nextTransactionId(), TransactionType.DEPOSIT,
                null, account, value, ZERO, Instant.now(clock), TransactionStatus.SUCCESS);
        bank.recordTransaction(tx);
        notificationService.notifySuccess(tx);
        return tx;
    }

    // ---------- step 1: create a request and send the OTP ----------
    public TransactionRequest requestWithdrawal(Customer customer, String accountNumber, BigDecimal amount) {
        BigDecimal value = normalizeAmount(amount);
        Account source = findOwnedAccount(customer, accountNumber);
        if (!source.hasSufficientFunds(value)) {
            throw new InsufficientFundsException("Insufficient funds in account " + accountNumber + ".");
        }
        return startRequest(customer, TransactionType.WITHDRAWAL, source, null, value, ZERO);
    }

    public TransactionRequest requestTransfer(Customer customer, String fromNumber, String toNumber,
                                              BigDecimal amount) {
        BigDecimal value = normalizeAmount(amount);
        Account source = findOwnedAccount(customer, fromNumber);
        Account destination = bank.findAccount(toNumber);
        if (destination.getRole() != AccountRole.CUSTOMER) {
            throw new InvalidTransferException("You cannot transfer to this account.");
        }
        if (source == destination) {
            throw new InvalidTransferException("Sender and receiver accounts must be different.");
        }
        TransferFeePolicy policy = source.hasSameOwnerAs(destination) ? ownAccountFeePolicy : feePolicy;
        BigDecimal fee = policy.calculateFee(value);
        if (!source.hasSufficientFunds(value.add(fee))) {
            throw new InsufficientFundsException("Insufficient funds. You need the amount plus the fee ("
                    + value.add(fee).toPlainString() + ").");
        }
        return startRequest(customer, TransactionType.TRANSFER, source, destination, value, fee);
    }

    // ---------- step 2: verify the OTP ----------
    public void verifyOtp(Customer customer, String requestId, String submittedOtp) {
        TransactionRequest request = findOwnedRequest(customer, requestId);
        requireStatus(request, RequestStatus.PENDING_OTP);
        failIfExpired(request);
        try {
            otpService.verify(requestId, customer.getCustomerId(), submittedOtp);
            request.setStatus(RequestStatus.PENDING_CONFIRMATION);
        } catch (OtpExpiredException e) {
            failRequest(request, e.getMessage());
            throw e;
        } catch (InvalidOtpException e) {
            if (e.getRemainingAttempts() == 0) {
                failRequest(request, e.getMessage());
            }
            throw e;
        }
    }

    // ---------- step 3: customer confirms (or cancels) ----------
    public Transaction confirm(Customer customer, String requestId) {
        TransactionRequest request = findOwnedRequest(customer, requestId);
        requireStatus(request, RequestStatus.PENDING_CONFIRMATION);
        failIfExpired(request);
        try {
            Transaction tx = execute(request);
            request.setStatus(RequestStatus.SUCCESS);
            bank.recordTransaction(tx);
            notificationService.notifySuccess(tx);
            return tx;
        } catch (BankException e) {
            failRequest(request, e.getMessage());
            throw e;
        }
    }

    public void cancel(Customer customer, String requestId) {
        TransactionRequest request = findOwnedRequest(customer, requestId);
        if (request.getStatus() != RequestStatus.PENDING_OTP
                && request.getStatus() != RequestStatus.PENDING_CONFIRMATION) {
            throw new BankException("This request can no longer be cancelled.");
        }
        request.setStatus(RequestStatus.CANCELLED);
        bank.recordTransaction(buildRecord(request, TransactionStatus.CANCELLED));
        notificationService.notifyCancelled(request);
    }

    // ---------- reports ----------
    public List<Transaction> getHistory(Customer customer, String accountNumber) {
        return bank.getTransactionsFor(findOwnedAccount(customer, accountNumber));
    }

    public Account getBankFeeAccount() {
        return bank.getBankFeeAccount();
    }

    public List<Transaction> getFeeTransactions() {
        List<Transaction> result = new ArrayList<>();
        for (Transaction tx : bank.getAllTransactions()) {
            if (tx.getType() == TransactionType.TRANSFER && tx.getStatus() == TransactionStatus.SUCCESS && tx.getFee().signum() > 0) {
                result.add(tx);
            }
        }
        return result;
    }

    // ---------- private helpers ----------
    private TransactionRequest startRequest(Customer customer, TransactionType type, Account source,
                                            Account destination, BigDecimal amount, BigDecimal fee) {
        Instant now = Instant.now(clock);
        TransactionRequest request = new TransactionRequest(bank.nextRequestId(), customer, type,
                source, destination, amount, fee, now, now.plusSeconds(requestExpirySeconds));
        bank.saveRequest(request);
        String otp = otpService.generateOtp(request.getRequestId(), customer.getCustomerId());
        notificationService.sendOtp(customer, request.getRequestId(), otp, otpExpirySeconds);
        return request;
    }

    /** All checks happen before the first balance change, so a failure cannot leave a partial update. */
    private Transaction execute(TransactionRequest request) {
        Account source = request.getSource();
        BigDecimal total = request.getTotalDeduction();
        if (!source.hasSufficientFunds(total)) {
            throw new InsufficientFundsException("Insufficient funds for this transaction.");
        }
        source.withdraw(total);
        if (request.getType() == TransactionType.TRANSFER) {
            request.getDestination().deposit(request.getAmount());
            if (request.getFee().signum() > 0) {
                bank.getBankFeeAccount().deposit(request.getFee());
            }
        }
        return buildRecord(request, TransactionStatus.SUCCESS);
    }

    private Transaction buildRecord(TransactionRequest request, TransactionStatus status) {
        return new Transaction(bank.nextTransactionId(), request.getType(), request.getSource(),
                request.getDestination(), request.getAmount(), request.getFee(), Instant.now(clock), status);
    }

    private void failRequest(TransactionRequest request, String reason) {
        if (request.getStatus() == RequestStatus.FAILED || request.getStatus() == RequestStatus.SUCCESS
                || request.getStatus() == RequestStatus.CANCELLED) {
            return;
        }
        request.setStatus(RequestStatus.FAILED);
        bank.recordTransaction(buildRecord(request, TransactionStatus.FAILED));
        notificationService.notifyFailure(request, reason);
    }

    private void failIfExpired(TransactionRequest request) {
        if (request.isExpired(Instant.now(clock))) {
            failRequest(request, "The request expired.");
            throw new BankException("This request has expired. Please start again.");
        }
    }

    private void requireStatus(TransactionRequest request, RequestStatus expected) {
        if (request.getStatus() != expected) {
            throw new BankException("This request is not waiting for that step (status: "
                    + request.getStatus() + ").");
        }
    }

    private Account findOwnedAccount(Customer customer, String accountNumber) {
        Account account = bank.findAccount(accountNumber);
        if (!account.isOwnedBy(customer)) {
            throw new BankException("Account " + accountNumber + " does not belong to you.");
        }
        return account;
    }

    private TransactionRequest findOwnedRequest(Customer customer, String requestId) {
        TransactionRequest request = bank.findRequest(requestId);
        if (!request.getCustomer().getCustomerId().equals(customer.getCustomerId())) {
            throw new BankException("Request not found.");
        }
        return request;
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new InvalidAmountException("Amount can have at most 2 decimal places.");
        }
        return amount.setScale(2, RoundingMode.UNNECESSARY);
    }
}
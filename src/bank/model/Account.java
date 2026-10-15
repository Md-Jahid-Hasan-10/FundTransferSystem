package bank.model;

import bank.exception.InsufficientFundsException;
import bank.exception.InvalidAmountException;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Account {
    private final String accountNumber;
    private final Customer owner;          // null for the bank's fee account
    private final AccountRole role;
    private final BigDecimal minimumBalance;
    private BigDecimal balance = new BigDecimal("0.00");
    private AccountStatus status = AccountStatus.ACTIVE;

    public Account(String accountNumber, Customer owner, AccountRole role) {
        this(accountNumber, owner, role, new BigDecimal("0.00"));
    }

    public Account(String accountNumber, Customer owner, AccountRole role, BigDecimal minimumBalance) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.role = role;
        this.minimumBalance = minimumBalance.setScale(2, RoundingMode.HALF_UP);
    }

    public void deposit(BigDecimal amount) {
        requirePositive(amount);
        balance = balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        requirePositive(amount);
        if (!hasSufficientFunds(amount)) {
            throw new InsufficientFundsException("Insufficient funds in account " + accountNumber
                    + " (a minimum balance of " + minimumBalance.toPlainString() + " must remain).");
        }
        balance = balance.subtract(amount);
    }

    /** True if the balance after spending this amount still stays at or above the minimum balance. */
    public boolean hasSufficientFunds(BigDecimal amount) {
        return balance.subtract(amount).compareTo(minimumBalance) >= 0;
    }

    /** The most that can be spent right now while keeping the minimum balance. */
    public BigDecimal getAvailableToSpend() {
        return balance.subtract(minimumBalance).max(new BigDecimal("0.00"));
    }

    public boolean isOwnedBy(Customer customer) {
        return owner != null && owner.getCustomerId().equals(customer.getCustomerId());
    }

    public boolean hasSameOwnerAs(Account other) {
        return owner != null && other.owner != null
                && owner.getCustomerId().equals(other.owner.getCustomerId());
    }

    public void freeze() {
        status = AccountStatus.FROZEN;
    }

    public void unfreeze() {
        status = AccountStatus.ACTIVE;
    }

    public boolean isFrozen() {
        return status == AccountStatus.FROZEN;
    }

    private void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
    }

    public BigDecimal getBalance() { return balance; }
    public BigDecimal getMinimumBalance() { return minimumBalance; }
    public String getAccountNumber() { return accountNumber; }
    public Customer getOwner() { return owner; }
    public AccountRole getRole() { return role; }
    public AccountStatus getStatus() { return status; }
}
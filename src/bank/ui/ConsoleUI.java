package bank.ui;

import bank.config.AppConfig;
import bank.core.Bank;
import bank.exception.BankException;
import bank.exception.InvalidAmountException;
import bank.exception.InvalidOtpException;
import bank.exception.OtpExpiredException;
import bank.model.Account;
import bank.model.Customer;
import bank.model.Transaction;
import bank.model.TransactionRequest;
import bank.model.TransactionType;
import bank.service.AdminService;
import bank.service.FundTransferService;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class ConsoleUI {
    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final Bank bank;
    private final FundTransferService service;
    private final AdminService adminService;
    private final AppConfig config;
    private final Scanner scanner = new Scanner(System.in);

    public ConsoleUI(Bank bank, FundTransferService service, AdminService adminService, AppConfig config) {
        this.bank = bank;
        this.service = service;
        this.adminService = adminService;
        this.config = config;
    }

    public void start() {
        System.out.println("=== Fund Transfer System ===");
        boolean running = true;
        while (running) {
            System.out.println("\n1. Login");
            System.out.println("2. Register");
            System.out.println("3. Bank fee report");
            System.out.println("4. Admin login");
            System.out.println("0. Exit");
            String choice = prompt("Choose: ");
            try {
                switch (choice) {
                    case "1" -> login();
                    case "2" -> register();
                    case "3" -> showFeeReport();
                    case "4" -> adminLogin();
                    case "0" -> running = false;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (BankException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Goodbye!");
    }

    // ---------- start menu ----------
    private void login() {
        String id = prompt("Customer ID: ");
        String pin = prompt("PIN: ");
        Customer customer = bank.login(id, pin);
        System.out.println("Welcome, " + customer.getName() + "!");
        customerMenu(customer);
    }

    private void register() {
        String name = prompt("Name: ");
        String email = prompt("Email: ");
        String phone = prompt("Phone: ");
        String pin = prompt("Choose a PIN: ");
        Customer customer = bank.registerCustomer(name, email, phone, pin);
        Account account = bank.openAccount(customer);
        System.out.println("Registered! Your customer ID is " + customer.getCustomerId()
                + " and your first account is " + account.getAccountNumber() + ".");
    }

    private void showFeeReport() {
        Account feeAccount = service.getBankFeeAccount();
        System.out.println("\n--- Bank fee account " + feeAccount.getAccountNumber() + " ---");
        System.out.println("Total fees collected: " + config.formatMoney(feeAccount.getBalance()));
        List<Transaction> transfers = service.getFeeTransactions();
        System.out.println("Transfers with a fee: " + transfers.size());
        for (Transaction tx : transfers) {
            System.out.println("  " + tx.getTransactionId() + "  amount " + config.formatMoney(tx.getAmount())
                    + "  fee " + config.formatMoney(tx.getFee()));
        }
    }

    // ---------- admin menu ----------
    private void adminLogin() {
        String username = prompt("Admin username: ");
        String password = prompt("Admin password: ");
        adminService.login(username, password);
        System.out.println("Admin access granted.");
        adminMenu();
    }

    private void adminMenu() {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- Admin ---");
            System.out.println("1. View all customers");
            System.out.println("2. View all accounts");
            System.out.println("3. Freeze an account");
            System.out.println("4. Unfreeze an account");
            System.out.println("5. View all transactions");
            System.out.println("0. Logout");
            String choice = prompt("Choose: ");
            try {
                switch (choice) {
                    case "1" -> showAllCustomers();
                    case "2" -> showAllAccounts();
                    case "3" -> {
                        adminService.freezeAccount(prompt("Account number to freeze: "));
                        System.out.println("Account frozen.");
                    }
                    case "4" -> {
                        adminService.unfreezeAccount(prompt("Account number to unfreeze: "));
                        System.out.println("Account unfrozen.");
                    }
                    case "5" -> printTransactionTable(adminService.getAllTransactions());
                    case "0" -> loggedIn = false;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (BankException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void showAllCustomers() {
        System.out.printf("%n%-10s %-14s %-26s %s%n", "ID", "Name", "Email", "Phone");
        for (Customer c : adminService.getAllCustomers()) {
            System.out.printf("%-10s %-14s %-26s %s%n", c.getCustomerId(), c.getName(), c.getEmail(), c.getPhone());
        }
    }

    private void showAllAccounts() {
        System.out.printf("%n%-13s %-14s %-20s %-8s %12s%n", "Account", "Owner", "Role", "Status", "Balance");
        for (Account a : adminService.getAllAccounts()) {
            System.out.printf("%-13s %-14s %-20s %-8s %12s%n",
                    a.getAccountNumber(),
                    a.getOwner() == null ? "BANK" : a.getOwner().getName(),
                    a.getRole(),
                    a.getStatus(),
                    config.formatMoney(a.getBalance()));
        }
    }

    // ---------- customer menu ----------
    private void customerMenu(Customer customer) {
        boolean loggedIn = true;
        while (loggedIn) {
            System.out.println("\n--- " + customer.getName() + " ---");
            System.out.println("1. View my accounts");
            System.out.println("2. Open a new account");
            System.out.println("3. Deposit");
            System.out.println("4. Withdraw");
            System.out.println("5. Transfer");
            System.out.println("6. Transaction history");
            System.out.println("7. View my limits");
            System.out.println("0. Logout");
            String choice = prompt("Choose: ");
            try {
                switch (choice) {
                    case "1" -> showAccounts(customer);
                    case "2" -> openAccount(customer);
                    case "3" -> deposit(customer);
                    case "4" -> withdraw(customer);
                    case "5" -> transfer(customer);
                    case "6" -> showHistory(customer);
                    case "7" -> showLimits(customer);
                    case "0" -> loggedIn = false;
                    default -> System.out.println("Invalid choice.");
                }
            } catch (BankException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void showAccounts(Customer customer) {
        System.out.println("\nYour accounts:");
        for (Account account : bank.getAccountsOf(customer)) {
            System.out.println("  " + account.getAccountNumber() + "  balance "
                    + config.formatMoney(account.getBalance())
                    + (account.isFrozen() ? "  [FROZEN]" : ""));
        }
    }

    private void showLimits(Customer customer) {
        BigDecimal used = service.getTodaysOutgoingTotal(customer);
        BigDecimal remaining = config.getDailyLimit().subtract(used).max(BigDecimal.ZERO);
        System.out.println("\n--- Your limits ---");
        System.out.println("Per-transaction limit : " + config.formatMoney(config.getPerTransactionLimit()));
        System.out.println("Daily limit           : " + config.formatMoney(config.getDailyLimit()));
        System.out.println("Used today            : " + config.formatMoney(used));
        System.out.println("Remaining today       : " + config.formatMoney(remaining));
        System.out.println("Minimum balance       : " + config.formatMoney(config.getMinimumBalance())
                + " (each account must keep this)");
        System.out.println("Transfers between your own accounts do not count toward these limits.");
    }

    private void openAccount(Customer customer) {
        Account account = bank.openAccount(customer);
        System.out.println("New account opened: " + account.getAccountNumber());
    }

    private void deposit(Customer customer) {
        showAccounts(customer);
        String accountNumber = prompt("Account number: ");
        BigDecimal amount = readAmount();
        Transaction tx = service.deposit(customer, accountNumber, amount);
        System.out.println("Deposit successful. Transaction ID: " + tx.getTransactionId());
    }

    private void withdraw(Customer customer) {
        showAccounts(customer);
        String accountNumber = prompt("Account number: ");
        BigDecimal amount = readAmount();
        TransactionRequest request = service.requestWithdrawal(customer, accountNumber, amount);
        completeProtectedFlow(customer, request);
    }

    private void transfer(Customer customer) {
        showAccounts(customer);
        String from = prompt("From account: ");
        String to = prompt("To account number: ");
        BigDecimal amount = readAmount();
        TransactionRequest request = service.requestTransfer(customer, from, to, amount);
        completeProtectedFlow(customer, request);
    }

    private void showHistory(Customer customer) {
        showAccounts(customer);
        String accountNumber = prompt("Account number: ");
        printTransactionTable(service.getHistory(customer, accountNumber));
    }

    private void printTransactionTable(List<Transaction> transactions) {
        if (transactions.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        System.out.printf("%n%-8s %-19s %-11s %-10s %-10s %12s %8s  %s%n",
                "ID", "Time", "Type", "From", "To", "Amount", "Fee", "Status");
        for (Transaction tx : transactions) {
            System.out.printf("%-8s %-19s %-11s %-10s %-10s %12s %8s  %s%n",
                    tx.getTransactionId(),
                    TIME_FORMAT.format(tx.getTimestamp()),
                    tx.getType(),
                    tx.getSource() == null ? "-" : tx.getSource().getAccountNumber(),
                    tx.getDestination() == null ? "-" : tx.getDestination().getAccountNumber(),
                    config.formatMoney(tx.getAmount()),
                    config.formatMoney(tx.getFee()),
                    tx.getStatus());
        }
    }

    // ---------- OTP + confirmation ----------
    private void completeProtectedFlow(Customer customer, TransactionRequest request) {
        System.out.println("\nAn OTP was sent to you for request " + request.getRequestId() + ".");
        if (!verifyOtpLoop(customer, request)) {
            return;
        }
        printSummary(request);
        String answer = prompt("Are you sure you want to complete this transaction? (yes/no): ");
        if (answer.equalsIgnoreCase("yes")) {
            Transaction tx = service.confirm(customer, request.getRequestId());
            System.out.println("Transaction successful. ID: " + tx.getTransactionId());
        } else {
            service.cancel(customer, request.getRequestId());
            System.out.println("Transaction cancelled. No money was moved.");
        }
    }

    private boolean verifyOtpLoop(Customer customer, TransactionRequest request) {
        while (true) {
            String otp = prompt("Enter OTP (or type 'cancel'): ");
            if (otp.equalsIgnoreCase("cancel")) {
                service.cancel(customer, request.getRequestId());
                System.out.println("Transaction cancelled. No money was moved.");
                return false;
            }
            try {
                service.verifyOtp(customer, request.getRequestId(), otp);
                System.out.println("OTP verified.");
                return true;
            } catch (InvalidOtpException e) {
                System.out.println(e.getMessage());
                if (e.getRemainingAttempts() == 0) {
                    System.out.println("Too many wrong attempts. The request has failed.");
                    return false;
                }
            } catch (OtpExpiredException e) {
                System.out.println(e.getMessage() + " The request has failed.");
                return false;
            }
        }
    }

    private void printSummary(TransactionRequest request) {
        System.out.println("\n--- Please review ---");
        System.out.println("Type          : " + request.getType());
        System.out.println("From account  : " + request.getSource().getAccountNumber());
        if (request.getType() == TransactionType.TRANSFER) {
            System.out.println("To account    : " + request.getDestination().getAccountNumber()
                    + " (" + request.getDestination().getOwner().getName() + ")");
        }
        System.out.println("Amount        : " + config.formatMoney(request.getAmount()));
        if (request.getType() == TransactionType.TRANSFER
                && request.getSource().hasSameOwnerAs(request.getDestination())) {
            System.out.println("Note          : transfer between your own accounts");
        }
        System.out.println("Fee           : " + config.formatMoney(request.getFee()));
        System.out.println("Total deducted: " + config.formatMoney(request.getTotalDeduction()));
    }

    // ---------- input helpers ----------
    private String prompt(String message) {
        System.out.print(message);
        if (!scanner.hasNextLine()) {
            System.out.println();
            System.exit(0);
        }
        return scanner.nextLine().trim();
    }

    private BigDecimal readAmount() {
        String text = prompt("Amount: ");
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            throw new InvalidAmountException("Please enter a valid number.");
        }
    }
}
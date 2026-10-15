package bank.service;

import bank.core.Bank;
import bank.exception.BankException;
import bank.model.Account;
import bank.model.AccountRole;
import bank.model.Customer;
import bank.model.Transaction;

import java.util.Collection;
import java.util.List;

/** Operations for bank staff. */
public class AdminService {
    private final Bank bank;
    private final NotificationService notificationService;
    private final String adminUsername;
    private final String adminPassword;

    public AdminService(Bank bank, NotificationService notificationService,
                        String adminUsername, String adminPassword) {
        this.bank = bank;
        this.notificationService = notificationService;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    /** Returns normally if the credentials are correct; otherwise throws. */
    public void login(String username, String password) {
        if (!adminUsername.equals(username) || !adminPassword.equals(password)) {
            throw new BankException("Invalid admin username or password.");
        }
    }

    public Collection<Customer> getAllCustomers() {
        return bank.getAllCustomers();
    }

    public Collection<Account> getAllAccounts() {
        return bank.getAllAccounts();
    }

    public List<Transaction> getAllTransactions() {
        return bank.getAllTransactions();
    }

    public void freezeAccount(String accountNumber) {
        Account account = findCustomerAccount(accountNumber);
        if (account.isFrozen()) {
            throw new BankException("Account " + accountNumber + " is already frozen.");
        }
        account.freeze();
        notificationService.notifyAccountStatusChanged(account, true);
    }

    public void unfreezeAccount(String accountNumber) {
        Account account = findCustomerAccount(accountNumber);
        if (!account.isFrozen()) {
            throw new BankException("Account " + accountNumber + " is not frozen.");
        }
        account.unfreeze();
        notificationService.notifyAccountStatusChanged(account, false);
    }

    private Account findCustomerAccount(String accountNumber) {
        Account account = bank.findAccount(accountNumber);
        if (account.getRole() != AccountRole.CUSTOMER) {
            throw new BankException("The bank fee account cannot be frozen.");
        }
        return account;
    }
}
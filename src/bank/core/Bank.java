package bank.core;

import bank.exception.AccountNotFoundException;
import bank.exception.BankException;
import bank.model.Account;
import bank.model.AccountRole;
import bank.model.Customer;
import bank.model.Transaction;
import bank.model.TransactionRequest;

import java.util.*;

/** Stores bank data in memory and generates IDs. It does not run the transaction workflow. */
public class Bank {
    private final Map<String, Customer> customers = new LinkedHashMap<>();
    private final Map<String, Account> accounts = new LinkedHashMap<>();
    private final Map<String, TransactionRequest> requests = new LinkedHashMap<>();
    private final List<Transaction> transactions = new ArrayList<>();
    private final Account bankFeeAccount;

    private int customerCounter = 1000;
    private int accountCounter = 1000;
    private int requestCounter = 1000;
    private int transactionCounter = 1000;

    public Bank() {
        bankFeeAccount = new Account("BANK-FEE-001", null, AccountRole.BANK_FEE_COLLECTION);
        accounts.put(bankFeeAccount.getAccountNumber(), bankFeeAccount);
    }

    // ----- customers -----
    public Customer registerCustomer(String name, String email, String phone, String pin) {
        String id = "CUST" + (++customerCounter);
        Customer customer = new Customer(id, name, email, phone, pin);
        customers.put(id, customer);
        return customer;
    }

    public Customer findCustomer(String customerId) {
        Customer customer = customers.get(customerId);
        if (customer == null) {
            throw new BankException("Customer not found.");
        }
        return customer;
    }

    public Customer login(String customerId, String pin) {
        Customer customer = customers.get(customerId);
        if (customer == null || !customer.verifyPin(pin)) {
            throw new BankException("Invalid customer ID or PIN.");
        }
        return customer;
    }

    // ----- accounts -----
    public Account openAccount(Customer owner) {
        String number = "ACC" + (++accountCounter);
        Account account = new Account(number, owner, AccountRole.CUSTOMER);
        accounts.put(number, account);
        return account;
    }

    public Account findAccount(String accountNumber) {
        Account account = accounts.get(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account " + accountNumber + " was not found.");
        }
        return account;
    }

    public List<Account> getAccountsOf(Customer customer) {
        List<Account> result = new ArrayList<>();
        for (Account account : accounts.values()) {
            if (account.isOwnedBy(customer)) {
                result.add(account);
            }
        }
        return result;
    }

    public Account getBankFeeAccount() {
        return bankFeeAccount;
    }

    // ----- requests -----
    public String nextRequestId() {
        return "REQ" + (++requestCounter);
    }

    public void saveRequest(TransactionRequest request) {
        requests.put(request.getRequestId(), request);
    }

    public TransactionRequest findRequest(String requestId) {
        TransactionRequest request = requests.get(requestId);
        if (request == null) {
            throw new BankException("Request not found.");
        }
        return request;
    }

    // ----- transactions -----
    public String nextTransactionId() {
        return "TXN" + (++transactionCounter);
    }

    public void recordTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public List<Transaction> getTransactionsFor(Account account) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction tx : transactions) {
            if (tx.getSource() == account || tx.getDestination() == account) {
                result.add(tx);
            }
        }
        return result;
    }

    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public Collection<Customer> getAllCustomers() {
    }
}
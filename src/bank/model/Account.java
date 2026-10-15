package bank.model;

import bank.exception.InsufficientFundsException;
import bank.exception.InvalidAmountException;

import java.math.BigDecimal;

public class Account {
    private final String accountNumber;
    private final Customer owner;          // null for the bank's fee account
    private final AccountRole role;
    private BigDecimal balance = new BigDecimal("0.00");

    public Account(String accountNumber, Customer owner, AccountRole role) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.role = role;
    }

    public void deposit(BigDecimal amount) {
        requirePositive(amount);
        balance = balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        requirePositive(amount);
        if (!hasSufficientFunds(amount)) {
            throw new InsufficientFundsException("Insufficient funds in account " + accountNumber + ".");
        }
        balance = balance.subtract(amount);
    }

    public boolean hasSufficientFunds(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }

    public boolean isOwnedBy(Customer customer) {
        return owner != null && owner.getCustomerId().equals(customer.getCustomerId());
    }

    private void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero.");
        }
    }
    public boolean hasSameOwnerAs(Account other) {
        return owner != null && other.owner != null
                && owner.getCustomerId().equals(other.owner.getCustomerId());
    }
    public BigDecimal getBalance() { return balance; }
    public String getAccountNumber() { return accountNumber; }
    public Customer getOwner() { return owner; }
    public AccountRole getRole() { return role; }
}
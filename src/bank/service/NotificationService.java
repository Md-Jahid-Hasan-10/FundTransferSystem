package bank.service;

import bank.config.AppConfig;
import bank.model.Account;
import bank.model.Customer;
import bank.model.Transaction;
import bank.model.TransactionRequest;
import bank.notification.NotificationChannel;

/** Builds the messages. The NotificationChannel decides how they are delivered. */
public class NotificationService {
    private final NotificationChannel channel;
    private final AppConfig config;

    public NotificationService(NotificationChannel channel, AppConfig config) {
        this.channel = channel;
        this.config = config;
    }

    public void sendOtp(Customer customer, String requestId, String otp, long validSeconds) {
        send(customer, "Your OTP for request " + requestId + " is " + otp
                + ". It is valid for " + validSeconds + " seconds. Never share it.");
    }

    public void notifySuccess(Transaction tx) {
        switch (tx.getType()) {
            case DEPOSIT -> send(tx.getDestination().getOwner(),
                    "Deposit of " + money(tx) + " into " + tx.getDestination().getAccountNumber()
                            + " successful. Balance: " + config.formatMoney(tx.getDestination().getBalance()) + ".");
            case WITHDRAWAL -> send(tx.getSource().getOwner(),
                    "Withdrawal of " + money(tx) + " from " + tx.getSource().getAccountNumber()
                            + " successful. Balance: " + config.formatMoney(tx.getSource().getBalance()) + ".");
            case TRANSFER -> {
                send(tx.getSource().getOwner(),
                        "Transfer of " + money(tx) + " to " + tx.getDestination().getAccountNumber()
                                + " successful. Fee: " + config.formatMoney(tx.getFee())
                                + ". Balance: " + config.formatMoney(tx.getSource().getBalance()) + ".");
                if (!tx.getSource().hasSameOwnerAs(tx.getDestination())) {
                    send(tx.getDestination().getOwner(),
                            "You received " + money(tx) + " from " + tx.getSource().getAccountNumber()
                                    + ". Balance: " + config.formatMoney(tx.getDestination().getBalance()) + ".");
                }
            }
        }
    }

    public void notifyFailure(TransactionRequest request, String reason) {
        send(request.getCustomer(), "Your " + request.getType() + " request " + request.getRequestId()
                + " failed. Reason: " + reason + " No money was moved.");
    }

    public void notifyCancelled(TransactionRequest request) {
        send(request.getCustomer(), "Your " + request.getType() + " request " + request.getRequestId()
                + " was cancelled. No money was moved.");
    }

    public void notifyAccountStatusChanged(Account account, boolean frozen) {
        String text = frozen
                ? "frozen. You cannot use it until the bank unfreezes it."
                : "unfrozen. You can use it again.";
        send(account.getOwner(), "Your account " + account.getAccountNumber() + " has been " + text);
    }

    private String money(Transaction tx) {
        return config.formatMoney(tx.getAmount());
    }

    private void send(Customer customer, String message) {
        channel.send(customer.getName() + " (" + customer.getPhone() + ")", message);
    }
}
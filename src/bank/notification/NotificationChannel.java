package bank.notification;

public interface NotificationChannel {
    void send(String recipient, String message);
}
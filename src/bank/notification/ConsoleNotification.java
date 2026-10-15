package bank.notification;

public class ConsoleNotification implements NotificationChannel {
    @Override
    public void send(String recipient, String message) {
        System.out.println("\n[NOTIFICATION -> " + recipient + "] " + message);
    }
}
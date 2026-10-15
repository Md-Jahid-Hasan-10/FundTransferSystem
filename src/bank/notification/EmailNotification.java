package bank.notification;

public class EmailNotification implements NotificationChannel {

    public void send(String recipient, String message)
    {
        System.out.println("\n[EMAIL NOTIFICATION -> " + recipient + "] " + message);
    }
}

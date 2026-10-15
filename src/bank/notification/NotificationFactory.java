package bank.notification;

public class NotificationFactory {

    private NotificationFactory() {
    }

    public static NotificationChannel create(String type) {
        switch (type.toUpperCase()) {
            case "CONSOLE":
                return new ConsoleNotification();
            case "FILE":
                return new FileNotification();
            case "EMAIL" :
                return new EmailNotification();
            default:
                throw new IllegalArgumentException("Unknown notification channel: " + type);
        }
    }
}
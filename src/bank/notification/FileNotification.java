package bank.notification;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public class FileNotification implements NotificationChannel {
    private final Path file;

    public FileNotification() {
        this("notifications.log");
    }

    public FileNotification(String fileName) {
        this.file = Paths.get(fileName);
    }

    @Override
    public void send(String recipient, String message) {
        String line = LocalDateTime.now() + " | " + recipient + " | " + message + System.lineSeparator();
        try {
            Files.writeString(file, line, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Could not write notification: " + e.getMessage());
        }
    }
}
package bank.config;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Singleton holding shared, read-only application settings. */
public final class AppConfig {
    private static final AppConfig INSTANCE = new AppConfig();

    private final boolean fixedOtp = true;               // true = FixedOtpGenerator, false = RandomOtpGenerator
    private final String fixedOtpValue = "123456";
    private final long otpExpirySeconds = 120;
    private final long requestExpirySeconds = 300;
    private final int maxOtpAttempts = 3;
    private final String currencySymbol = "৳";
    private final String notificationChannel = "CONSOLE"; // CONSOLE or FILE

    private AppConfig() {
    }

    public static AppConfig getInstance() {
        return INSTANCE;
    }

    public String formatMoney(BigDecimal amount) {
        return currencySymbol + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public boolean isFixedOtp() { return fixedOtp; }
    public String getFixedOtpValue() { return fixedOtpValue; }
    public long getOtpExpirySeconds() { return otpExpirySeconds; }
    public long getRequestExpirySeconds() { return requestExpirySeconds; }
    public int getMaxOtpAttempts() { return maxOtpAttempts; }
    public String getCurrencySymbol() { return currencySymbol; }
    public String getNotificationChannel() { return notificationChannel; }
}
package bank.otp;

import java.security.SecureRandom;

public class RandomOtpGenerator implements OtpGenerator {
    private final SecureRandom random = new SecureRandom();

    @Override
    public String generate() {
        return String.format("%06d", random.nextInt(1_000_000));
    }
}
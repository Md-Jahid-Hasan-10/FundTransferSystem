package bank.service;

import bank.exception.InvalidOtpException;
import bank.exception.OtpExpiredException;
import bank.otp.OtpGenerator;

import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class OtpService {

    private static class OtpRecord {
        private final String otp;
        private final String customerId;
        private final Instant expiresAt;
        private int attempts = 0;
        private boolean used = false;

        private OtpRecord(String otp, String customerId, Instant expiresAt) {
            this.otp = otp;
            this.customerId = customerId;
            this.expiresAt = expiresAt;
        }
    }

    private final OtpGenerator generator;
    private final Clock clock;
    private final long expirySeconds;
    private final int maxAttempts;
    private final Map<String, OtpRecord> records = new HashMap<>();

    public OtpService(OtpGenerator generator, Clock clock, long expirySeconds, int maxAttempts) {
        this.generator = generator;
        this.clock = clock;
        this.expirySeconds = expirySeconds;
        this.maxAttempts = maxAttempts;
    }

    /** Creates an OTP tied to one request and one customer. */
    public String generateOtp(String requestId, String customerId) {
        String otp = generator.generate();
        Instant expiresAt = Instant.now(clock).plusSeconds(expirySeconds);
        records.put(requestId, new OtpRecord(otp, customerId, expiresAt));
        return otp;
    }

    /** Returns normally if the OTP is valid; otherwise throws. */
    public void verify(String requestId, String customerId, String submittedOtp) {
        OtpRecord record = records.get(requestId);
        if (record == null || record.used || !record.customerId.equals(customerId)) {
            throw new InvalidOtpException("No active OTP for this request.", 0);
        }
        if (Instant.now(clock).isAfter(record.expiresAt)) {
            records.remove(requestId);
            throw new OtpExpiredException("OTP has expired.");
        }
        if (record.attempts >= maxAttempts) {
            throw new InvalidOtpException("Maximum OTP attempts exceeded.", 0);
        }
        if (record.otp.equals(submittedOtp)) {
            record.used = true;
            return;
        }
        record.attempts++;
        int remaining = maxAttempts - record.attempts;
        String message = remaining == 0
                ? "Incorrect OTP. No attempts left."
                : "Incorrect OTP. Attempts left: " + remaining + ".";
        throw new InvalidOtpException(message, remaining);
    }
}
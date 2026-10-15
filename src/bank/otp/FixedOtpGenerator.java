package bank.otp;

public class FixedOtpGenerator implements OtpGenerator {
    private final String otp;

    public FixedOtpGenerator(String otp) {
        this.otp = otp;
    }

    @Override
    public String generate() {
        return otp;
    }
}
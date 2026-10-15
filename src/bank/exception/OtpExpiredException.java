package bank.exception;

public class OtpExpiredException extends BankException {
    public OtpExpiredException(String message) {
        super(message);
    }
}
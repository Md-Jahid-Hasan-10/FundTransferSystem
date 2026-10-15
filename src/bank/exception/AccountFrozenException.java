package bank.exception;

public class AccountFrozenException extends BankException {
    public AccountFrozenException(String message) {
        super(message);
    }
}
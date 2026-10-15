package bank.policy;

import java.math.BigDecimal;

public interface TransactionLimitPolicy {
    /**
     * Throws TransactionLimitExceededException if the amount is not allowed.
     * @param amount           the amount the customer wants to send out
     * @param alreadyUsedToday the total the customer has already sent out today
     */
    void check(BigDecimal amount, BigDecimal alreadyUsedToday);
}
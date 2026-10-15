package bank.policy;

import bank.exception.TransactionLimitExceededException;

import java.math.BigDecimal;

public class StandardLimitPolicy implements TransactionLimitPolicy {
    private final BigDecimal perTransactionLimit;
    private final BigDecimal dailyLimit;

    public StandardLimitPolicy(BigDecimal perTransactionLimit, BigDecimal dailyLimit) {
        this.perTransactionLimit = perTransactionLimit;
        this.dailyLimit = dailyLimit;
    }

    @Override
    public void check(BigDecimal amount, BigDecimal alreadyUsedToday) {
        if (amount.compareTo(perTransactionLimit) > 0) {
            throw new TransactionLimitExceededException(
                    "Amount exceeds the per-transaction limit of " + perTransactionLimit.toPlainString() + ".");
        }
        BigDecimal remaining = dailyLimit.subtract(alreadyUsedToday).max(BigDecimal.ZERO);
        if (amount.compareTo(remaining) > 0) {
            throw new TransactionLimitExceededException("Daily limit exceeded. Daily limit: "
                    + dailyLimit.toPlainString() + ", used today: " + alreadyUsedToday.toPlainString()
                    + ", remaining: " + remaining.toPlainString() + ".");
        }
    }
}
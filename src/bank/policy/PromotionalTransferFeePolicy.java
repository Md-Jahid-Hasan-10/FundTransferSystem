package bank.policy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Gives a percentage discount on top of another fee policy. */
public class PromotionalTransferFeePolicy implements TransferFeePolicy {
    private final TransferFeePolicy basePolicy;
    private final int discountPercent;

    public PromotionalTransferFeePolicy(TransferFeePolicy basePolicy, int discountPercent) {
        this.basePolicy = basePolicy;
        this.discountPercent = discountPercent;
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        return basePolicy.calculateFee(amount)
                .multiply(BigDecimal.valueOf(100 - discountPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
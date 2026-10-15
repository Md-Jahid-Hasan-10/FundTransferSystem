package bank.policy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Charges the same fixed fee for every transfer. Use a zero fee for "free". */
public class FlatTransferFeePolicy implements TransferFeePolicy {
    private final BigDecimal fee;

    public FlatTransferFeePolicy(BigDecimal fee) {
        if (fee.signum() < 0) {
            throw new IllegalArgumentException("Fee cannot be negative.");
        }
        this.fee = fee.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        return fee;
    }
}
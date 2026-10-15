package bank.policy;

import java.math.BigDecimal;

public interface TransferFeePolicy {
    BigDecimal calculateFee(BigDecimal amount);
}
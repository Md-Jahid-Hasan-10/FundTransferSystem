package bank.policy;

import java.math.BigDecimal;
import java.util.List;

public class StandardTransferFeePolicy implements TransferFeePolicy {

    private record Tier(BigDecimal maxAmount, BigDecimal fee) { }

    // Change these tiers to match your project document.
    private static final List<Tier> TIERS = List.of(
            new Tier(new BigDecimal("100.00"), new BigDecimal("5.00")),
            new Tier(new BigDecimal("1000.00"), new BigDecimal("9.25")),
            new Tier(new BigDecimal("10000.00"), new BigDecimal("15.00"))
    );
    private static final BigDecimal TOP_FEE = new BigDecimal("25.00");

    @Override
    public BigDecimal calculateFee(BigDecimal amount) {
        for (Tier tier : TIERS) {
            if (amount.compareTo(tier.maxAmount()) <= 0) {
                return tier.fee();
            }
        }
        return TOP_FEE;
    }
}
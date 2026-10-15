package bank.model;

import java.math.BigDecimal;
import java.time.Instant;

/** Immutable record of a finished (or failed/cancelled) operation. */
public final class Transaction {
    private final String transactionId;
    private final TransactionType type;
    private final Account source;        // null for deposits
    private final Account destination;   // null for withdrawals
    private final BigDecimal amount;
    private final BigDecimal fee;
    private final Instant timestamp;
    private final TransactionStatus status;

    public Transaction(String transactionId, TransactionType type, Account source, Account destination,
                       BigDecimal amount, BigDecimal fee, Instant timestamp, TransactionStatus status) {
        this.transactionId = transactionId;
        this.type = type;
        this.source = source;
        this.destination = destination;
        this.amount = amount;
        this.fee = fee;
        this.timestamp = timestamp;
        this.status = status;
    }

    public String getTransactionId() { return transactionId; }
    public TransactionType getType() { return type; }
    public Account getSource() { return source; }
    public Account getDestination() { return destination; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getFee() { return fee; }
    public Instant getTimestamp() { return timestamp; }
    public TransactionStatus getStatus() { return status; }
}
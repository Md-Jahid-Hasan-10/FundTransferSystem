package bank.model;

import java.math.BigDecimal;
import java.time.Instant;

/** A requested operation that is still moving through OTP and confirmation. */
public class TransactionRequest {
    private final String requestId;
    private final Customer customer;
    private final TransactionType type;
    private final Account source;
    private final Account destination;   // null for withdrawals
    private final BigDecimal amount;
    private final BigDecimal fee;
    private final Instant createdAt;
    private final Instant expiresAt;
    private RequestStatus status = RequestStatus.PENDING_OTP;

    public TransactionRequest(String requestId, Customer customer, TransactionType type, Account source,
                              Account destination, BigDecimal amount, BigDecimal fee,
                              Instant createdAt, Instant expiresAt) {
        this.requestId = requestId;
        this.customer = customer;
        this.type = type;
        this.source = source;
        this.destination = destination;
        this.amount = amount;
        this.fee = fee;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public BigDecimal getTotalDeduction() {
        return amount.add(fee);
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public String getRequestId() { return requestId; }
    public Customer getCustomer() { return customer; }
    public TransactionType getType() { return type; }
    public Account getSource() { return source; }
    public Account getDestination() { return destination; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getFee() { return fee; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
}
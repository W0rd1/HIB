package org.example.bankapi.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name="payment_requests")
public class PaymentRequest {
    public enum Status { PENDING, PAID, REJECTED, EXPIRED }
    @Id private UUID id;
    @Version private long version;
    @ManyToOne(optional=false) private Customer requester;
    @ManyToOne(optional=false) private Customer payer;
    @Column(nullable=false, precision=19, scale=2) private BigDecimal amount;
    @Column(length=500) private String description;
    private boolean editable;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private Status status=Status.PENDING;
    @Column(nullable=false) private Instant createdAt;
    @Column(nullable=false) private Instant expiresAt;
    protected PaymentRequest() {}
    public PaymentRequest(Customer requester, Customer payer, BigDecimal amount, String description, boolean editable) {
        this.id=UUID.randomUUID(); this.requester=requester; this.payer=payer; this.amount=amount; this.description=description;
        this.editable=editable; this.createdAt=Instant.now(); this.expiresAt=createdAt.plusSeconds(1800);
    }
    public UUID getId(){return id;} public Customer getRequester(){return requester;} public Customer getPayer(){return payer;}
    public BigDecimal getAmount(){return amount;} public String getDescription(){return description;} public boolean isEditable(){return editable;}
    public Status getStatus(){return status;} public void setStatus(Status status){this.status=status;}
    public Instant getCreatedAt(){return createdAt;} public Instant getExpiresAt(){return expiresAt;}
}

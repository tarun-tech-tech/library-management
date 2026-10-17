package com.smartlib.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fines", uniqueConstraints = @UniqueConstraint(name = "uk_fines_loan", columnNames = "loan_id"))
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "amount_paid", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "days_overdue", nullable = false)
    private int daysOverdue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FineStatus status = FineStatus.UNPAID;

    @Column(name = "assessed_at", nullable = false)
    private Instant assessedAt;

    @Column(name = "paid_at")
    private Instant paidAt;

    protected Fine() {
    }

    public Fine(Loan loan, BigDecimal amount, int daysOverdue) {
        this.loan = loan;
        this.amount = amount;
        this.daysOverdue = daysOverdue;
    }

    @jakarta.persistence.PrePersist
    void onCreate() { assessedAt = Instant.now(); }

    public Long getId() { return id; }
    public Loan getLoan() { return loan; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public BigDecimal getBalance() { return amount.subtract(amountPaid); }
    public int getDaysOverdue() { return daysOverdue; }
    public FineStatus getStatus() { return status; }
    public Instant getAssessedAt() { return assessedAt; }
    public Instant getPaidAt() { return paidAt; }

    public void markPaid() {
        recordPayment(getBalance());
    }

    public void recordPayment(BigDecimal paymentAmount) {
        if (paymentAmount == null || paymentAmount.signum() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if (paymentAmount.compareTo(getBalance()) > 0) {
            throw new IllegalArgumentException("Payment amount cannot exceed the outstanding fine balance");
        }
        amountPaid = amountPaid.add(paymentAmount);
        status = amountPaid.compareTo(amount) == 0 ? FineStatus.PAID : FineStatus.PARTIALLY_PAID;
        if (status == FineStatus.PAID) paidAt = Instant.now();
    }
}

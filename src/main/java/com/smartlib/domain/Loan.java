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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_copy_id", nullable = false)
    private BookCopy bookCopy;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "returned_at")
    private LocalDate returnedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LoanStatus status = LoanStatus.ISSUED;

    @Column(name = "renewal_count", nullable = false)
    private int renewalCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Loan() {
    }

    public Loan(BookCopy bookCopy, Member member, LocalDate issueDate, LocalDate dueDate) {
        this.bookCopy = bookCopy;
        this.member = member;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
    }

    @jakarta.persistence.PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @jakarta.persistence.PreUpdate
    void onUpdate() { updatedAt = Instant.now(); }

    public Long getId() { return id; }
    public BookCopy getBookCopy() { return bookCopy; }
    public Member getMember() { return member; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnedAt() { return returnedAt; }
    public LoanStatus getStatus() { return status; }
    public int getRenewalCount() { return renewalCount; }
    public boolean isIssued() { return status == LoanStatus.ISSUED; }
    public boolean isOverdue(LocalDate date) { return isIssued() && date.isAfter(dueDate); }

    public void renew(LocalDate newDueDate) {
        if (!isIssued()) throw new IllegalStateException("Only issued loans can be renewed");
        dueDate = newDueDate;
        renewalCount++;
    }

    public void markReturned(LocalDate returnedAt) {
        if (!isIssued()) throw new IllegalStateException("Loan has already been returned");
        this.returnedAt = returnedAt;
        this.status = LoanStatus.RETURNED;
    }
}

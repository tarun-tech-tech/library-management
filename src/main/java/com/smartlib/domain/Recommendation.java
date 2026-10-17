package com.smartlib.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "recommendations")
public class Recommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal score;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(name = "generated_at", nullable = false)
    private Instant generatedAt;

    protected Recommendation() {
    }

    public Recommendation(Member member, Book book, BigDecimal score, String reason) {
        this.member = member;
        this.book = book;
        this.score = score;
        this.reason = reason;
    }

    @jakarta.persistence.PrePersist
    void onCreate() { generatedAt = Instant.now(); }

    public Long getId() { return id; }
    public Member getMember() { return member; }
    public Book getBook() { return book; }
    public BigDecimal getScore() { return score; }
    public String getReason() { return reason; }
    public Instant getGeneratedAt() { return generatedAt; }
}

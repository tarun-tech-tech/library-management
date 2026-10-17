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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;

@Entity
@Table(name = "book_copies", uniqueConstraints = @UniqueConstraint(name = "uk_book_copies_inventory_code", columnNames = "inventory_code"))
public class BookCopy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "inventory_code", nullable = false, length = 80)
    private String inventoryCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CopyStatus status = CopyStatus.AVAILABLE;

    @Column(name = "acquired_at", nullable = false)
    private LocalDate acquiredAt;

    protected BookCopy() {
    }

    public BookCopy(Book book, String inventoryCode, LocalDate acquiredAt) {
        this.book = book;
        this.inventoryCode = inventoryCode;
        this.acquiredAt = acquiredAt;
    }

    public Long getId() { return id; }
    public Book getBook() { return book; }
    public String getInventoryCode() { return inventoryCode; }
    public CopyStatus getStatus() { return status; }
    public LocalDate getAcquiredAt() { return acquiredAt; }
    public void setStatus(CopyStatus status) { this.status = status; }
}

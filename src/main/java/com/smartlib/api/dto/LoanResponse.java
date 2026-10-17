package com.smartlib.api.dto;

import com.smartlib.domain.LoanStatus;
import java.math.BigDecimal;
import java.time.LocalDate;

public record LoanResponse(
        Long id,
        Long bookId,
        String bookTitle,
        Long bookCopyId,
        String inventoryCode,
        Long memberId,
        String memberName,
        LocalDate issueDate,
        LocalDate dueDate,
        LocalDate returnedAt,
        LoanStatus status,
        int renewalCount,
        BigDecimal fineAmount,
        int overdueDays) {
}

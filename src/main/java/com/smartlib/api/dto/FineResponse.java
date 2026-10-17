package com.smartlib.api.dto;

import com.smartlib.domain.FineStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record FineResponse(
        Long id,
        Long loanId,
        Long memberId,
        String memberName,
        BigDecimal amount,
        BigDecimal amountPaid,
        BigDecimal balance,
        int daysOverdue,
        FineStatus status,
        Instant assessedAt,
        Instant paidAt) {
}

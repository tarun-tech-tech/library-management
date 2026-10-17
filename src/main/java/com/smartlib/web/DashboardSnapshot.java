package com.smartlib.web;

import com.smartlib.api.dto.BookResponse;
import com.smartlib.api.dto.LoanResponse;
import java.math.BigDecimal;
import java.util.List;

public record DashboardSnapshot(
        long totalTitles,
        long totalCopies,
        long availableCopies,
        long activeMembers,
        long activeLoans,
        long overdueLoans,
        BigDecimal outstandingFines,
        List<BookResponse> books,
        List<LoanResponse> loans) {
}

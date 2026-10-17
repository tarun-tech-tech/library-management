package com.smartlib.api.dto;

import java.math.BigDecimal;

public record RecommendationResponse(
        Long bookId,
        String title,
        String author,
        String category,
        int availableCopies,
        BigDecimal score,
        String reason) {
}

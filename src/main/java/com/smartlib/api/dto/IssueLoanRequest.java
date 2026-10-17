package com.smartlib.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record IssueLoanRequest(
        @NotNull Long bookCopyId,
        @NotNull Long memberId,
        @Min(1) @Max(60) Integer durationDays) {
}

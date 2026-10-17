package com.smartlib.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record RenewLoanRequest(
        @Min(1) @Max(60) Integer additionalDays) {
}

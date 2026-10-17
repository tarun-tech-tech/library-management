package com.smartlib.api.dto;

import com.smartlib.domain.CopyStatus;
import jakarta.validation.constraints.NotNull;

public record BookCopyStatusRequest(@NotNull CopyStatus status) {
}

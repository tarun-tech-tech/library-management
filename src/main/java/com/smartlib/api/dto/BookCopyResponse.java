package com.smartlib.api.dto;

import com.smartlib.domain.CopyStatus;
import java.time.LocalDate;

public record BookCopyResponse(
        Long id,
        Long bookId,
        String inventoryCode,
        CopyStatus status,
        LocalDate acquiredAt) {
}

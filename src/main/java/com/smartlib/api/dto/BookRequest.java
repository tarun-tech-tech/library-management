package com.smartlib.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BookRequest(
        @NotBlank @Size(max = 240) String title,
        @NotBlank @Size(max = 180) String author,
        @NotBlank @Size(max = 20) String isbn,
        @NotBlank @Size(max = 120) String category,
        @Size(max = 180) String publisher,
        @Min(1000) Integer publicationYear,
        @NotNull @Min(1) Integer copies) {
}

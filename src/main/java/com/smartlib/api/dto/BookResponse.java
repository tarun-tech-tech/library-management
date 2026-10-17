package com.smartlib.api.dto;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        String category,
        String publisher,
        Integer publicationYear,
        int totalCopies,
        int availableCopies) {
}

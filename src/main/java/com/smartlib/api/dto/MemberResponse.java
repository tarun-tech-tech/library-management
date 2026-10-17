package com.smartlib.api.dto;

import java.time.LocalDate;

public record MemberResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        String address,
        LocalDate registrationDate,
        boolean active) {
}

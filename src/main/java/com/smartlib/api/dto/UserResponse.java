package com.smartlib.api.dto;

import com.smartlib.domain.Role;

public record UserResponse(
        Long id,
        String username,
        String fullName,
        String email,
        Role role,
        Long memberId,
        boolean enabled) {
}

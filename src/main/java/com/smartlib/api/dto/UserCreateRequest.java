package com.smartlib.api.dto;

import com.smartlib.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank @Size(min = 3, max = 80) String username,
        @NotBlank @Size(min = 16, max = 200) String password,
        @NotBlank @Size(max = 160) String fullName,
        @NotBlank @Email @Size(max = 255) String email,
        @NotNull Role role,
        Long memberId) {
}

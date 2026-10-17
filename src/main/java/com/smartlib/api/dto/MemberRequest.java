package com.smartlib.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MemberRequest(
        @NotBlank @Size(max = 160) String fullName,
        @NotBlank @Email @Size(max = 255) String email,
        @Size(max = 40) String phone,
        @Size(max = 500) String address) {
}

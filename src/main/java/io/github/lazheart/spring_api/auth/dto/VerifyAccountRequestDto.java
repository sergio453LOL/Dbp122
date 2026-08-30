package io.github.lazheart.spring_api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record VerifyAccountRequestDto(
        @NotBlank @Email String email,
        @NotBlank String verificationCode
) {}

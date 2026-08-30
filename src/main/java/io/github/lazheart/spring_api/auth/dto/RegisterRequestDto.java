package io.github.lazheart.spring_api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO para el registro de un nuevo usuario.
 */
public record RegisterRequestDto(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotBlank String phoneNumber
) {}

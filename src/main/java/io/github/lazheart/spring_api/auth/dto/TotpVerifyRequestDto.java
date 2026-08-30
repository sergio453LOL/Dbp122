package io.github.lazheart.spring_api.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO para verificar el código TOTP cuando el usuario ya se autenticó con contraseña
 * pero tiene 2FA habilitado.
 */
public record TotpVerifyRequestDto(
        @NotBlank String identifier,
        @NotBlank String password,
        @NotBlank String totpCode
) {}

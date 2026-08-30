package io.github.lazheart.spring_api.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO de login.
 * Se puede usar {@code username} o {@code email} para identificar al usuario.
 * Si el usuario tiene 2FA activo, {@code totpCode} debe incluirse en la misma llamada.
 */
public record AuthRequestDto(
        /** Puede ser el username o el email */
        String identifier,
        @NotBlank String password,
        /** Código TOTP de 6 dígitos (requerido si 2FA está habilitado) */
        String totpCode
) {}
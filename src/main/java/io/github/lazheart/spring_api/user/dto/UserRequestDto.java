package io.github.lazheart.spring_api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * DTO para actualizar datos del usuario autenticado.
 * Todos los campos son opcionales (null = no modificar).
 */
public record UserRequestDto(
        @Size(min = 3, max = 50) String username,
        @Email String email,
        @Size(min = 7, max = 20) String phoneNumber
) {}

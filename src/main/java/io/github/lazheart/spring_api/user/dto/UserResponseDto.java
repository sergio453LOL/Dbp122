package io.github.lazheart.spring_api.user.dto;

import io.github.lazheart.spring_api.user.domain.Role;

import java.util.UUID;

/**
 * DTO de respuesta completo (para ADMIN).
 * Incluye id, username, email, phoneNumber y role.
 */
public record UserResponseDto(
        UUID id,
        String username,
        String email,
        String phoneNumber,
        Role role
) {}

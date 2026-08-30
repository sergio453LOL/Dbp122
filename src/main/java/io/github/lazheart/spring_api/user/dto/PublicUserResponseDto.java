package io.github.lazheart.spring_api.user.dto;

/**
 * DTO de vista pública de usuario (para USER).
 * Solo expone username y email, sin ID.
 */
public record PublicUserResponseDto(
        String username,
        String email
) {}

package io.github.lazheart.spring_api.auth.dto;

/**
 * Respuesta al solicitar el QR de inicio de sesión.
 * Contiene la imagen PNG en Base64 y el token de sesión QR.
 */
public record QrLoginResponseDto(
        /** Imagen QR en Base64 (PNG) */
        String qrImageBase64,
        /** Token QR temporal (UUID) que se escanea */
        String qrSessionToken,
        String message
) {}

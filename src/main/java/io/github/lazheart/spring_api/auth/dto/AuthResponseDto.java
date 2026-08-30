package io.github.lazheart.spring_api.auth.dto;

/**
 * Respuesta estándar de autenticación.
 *
 * @param token        JWT Bearer token (nulo si se requiere 2FA)
 * @param totpRequired true cuando la contraseña fue correcta pero falta el código TOTP
 * @param message      Mensaje informativo
 */
public record AuthResponseDto(
        String token,
        boolean totpRequired,
        String message
) {
    public static AuthResponseDto success(String token) {
        return new AuthResponseDto(token, false, "Autenticación exitosa");
    }

    public static AuthResponseDto requireTotp() {
        return new AuthResponseDto(null, true, "Se requiere código TOTP");
    }

    public static AuthResponseDto of(String token, String message) {
        return new AuthResponseDto(token, false, message);
    }
}

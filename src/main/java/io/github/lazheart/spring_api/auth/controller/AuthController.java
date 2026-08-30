package io.github.lazheart.spring_api.auth.controller;

import io.github.lazheart.spring_api.auth.dto.*;
import io.github.lazheart.spring_api.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Rutas públicas de autenticación:
 * POST /api/auth/register    → registro
 * POST /api/auth/login       → login (username o email, con 2FA opcional)
 * POST /api/auth/qr/consume  → canjear token QR por JWT (ruta pública)
 *
 * Rutas protegidas (requieren JWT):
 * POST /api/auth/2fa/enable  → activar 2FA
 * POST /api/auth/2fa/disable → desactivar 2FA
 * POST /api/auth/qr/generate → generar QR de sesión
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro, login y gestión de 2FA / QR")
public class AuthController {

    private final AuthService authService;

    // ---- PÚBLICAS ----

    @PostMapping("/register")
    @Operation(summary = "Registro de nuevo usuario")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {
        return ResponseEntity.ok(authService.register(dto));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verificar cuenta con el código de correo")
    public ResponseEntity<AuthResponseDto> verifyAccount(@Valid @RequestBody VerifyAccountRequestDto dto) {
        return ResponseEntity.ok(authService.verifyAccount(dto));
    }

    @PostMapping("/login")
    @Operation(summary = "Login con username o email (+ TOTP si 2FA está activo)")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    /**
     * El cliente escanea el QR y llama a este endpoint con el token extraído.
     * Devuelve un JWT listo para usar.
     */
    @PostMapping("/qr/consume")
    @Operation(summary = "Consumir token QR para obtener JWT")
    public ResponseEntity<AuthResponseDto> consumeQr(@RequestParam String qrSessionToken) {
        return ResponseEntity.ok(authService.loginWithQrToken(qrSessionToken));
    }

    // ---- PROTEGIDAS (usuario autenticado) ----

    @PostMapping("/2fa/enable")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Activar autenticación de doble factor (2FA)")
    public ResponseEntity<String> enable2fa(@AuthenticationPrincipal UserDetails userDetails) {
        String otpAuthUrl = authService.enable2fa(userDetails.getUsername());
        return ResponseEntity.ok(otpAuthUrl);
    }

    @PostMapping("/2fa/disable")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Desactivar autenticación de doble factor (2FA)")
    public ResponseEntity<String> disable2fa(@AuthenticationPrincipal UserDetails userDetails) {
        authService.disable2fa(userDetails.getUsername());
        return ResponseEntity.ok("2FA desactivado correctamente");
    }

    /**
     * El usuario ya autenticado genera un QR que puede escanear desde otro
     * dispositivo/cliente para iniciar sesión sin contraseña.
     */
    @PostMapping("/qr/generate")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Generar QR de sesión para login rápido")
    public ResponseEntity<QrLoginResponseDto> generateQr(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.generateQrSessionCode(userDetails.getUsername()));
    }
}

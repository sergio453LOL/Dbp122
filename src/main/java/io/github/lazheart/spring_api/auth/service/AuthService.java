package io.github.lazheart.spring_api.auth.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import io.github.lazheart.spring_api.auth.dto.*;
import io.github.lazheart.spring_api.config.jwt.JwtService;
import io.github.lazheart.spring_api.mail.service.MailService;
import io.github.lazheart.spring_api.user.domain.Role;
import io.github.lazheart.spring_api.user.domain.User;
import io.github.lazheart.spring_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * Servicio de autenticación:
 * - Registro de usuarios
 * - Login con username o email (+ 2FA TOTP)
 * - Generación de QR de sesión para login rápido
 * - Consumo del QR para obtener JWT
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final MailService mailService;

    // =========================================================
    // REGISTRO
    // =========================================================

    /**
     * Registra un nuevo usuario con rol USER.
     * Genera automáticamente el secreto TOTP para 2FA.
     */
    @Transactional
    public AuthResponseDto register(RegisterRequestDto dto) {
        if (userRepository.existsByUsername(dto.username())) {
            throw new IllegalArgumentException("El username ya está en uso");
        }
        if (userRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("El email ya está en uso");
        }
        if (userRepository.existsByPhoneNumber(dto.phoneNumber())) {
            throw new IllegalArgumentException("El número de teléfono ya está en uso");
        }

        SecretGenerator secretGenerator = new DefaultSecretGenerator();
        String totpSecret = secretGenerator.generate();

        User user = new User();
        user.setUsername(dto.username());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setPhoneNumber(dto.phoneNumber());
        user.setRole(Role.USER);
        user.setTwoFactorSecret(totpSecret);
        user.setTwoFactorEnabled(false);
        user.setEnabled(false);
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiresAt(LocalDateTime.now().plusMinutes(15));

        userRepository.save(user);

        mailService.sendVerificationEmail(user.getEmail(), user.getUsername(), user.getVerificationCode());

        return AuthResponseDto.of(null, "Usuario registrado exitosamente. Por favor verifica tu cuenta con el código enviado a tu correo.");
    }

    /**
     * Verifica la cuenta de usuario con el código enviado al correo.
     */
    @Transactional
    public AuthResponseDto verifyAccount(VerifyAccountRequestDto dto) {
        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (user.isEnabled()) {
            throw new IllegalStateException("La cuenta ya está verificada");
        }
        if (user.getVerificationCode() == null || !user.getVerificationCode().equals(dto.verificationCode())) {
            throw new IllegalArgumentException("Código de verificación incorrecto");
        }
        if (user.getVerificationCodeExpiresAt() != null && user.getVerificationCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("El código de verificación ha expirado");
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiresAt(null);
        userRepository.save(user);

        mailService.sendVerificationSuccessEmail(user.getEmail(), user.getUsername());
        mailService.sendWelcomeEmail(user.getEmail(), user.getUsername());

        String token = jwtService.generateToken(user);
        return AuthResponseDto.success(token);
    }

    // =========================================================
    // LOGIN
    // =========================================================

    /**
     * Autentica al usuario con su contraseña.
     * El {@code identifier} puede ser username o email.
     * Si tiene 2FA activo, debe pasar {@code totpCode}.
     */
    public AuthResponseDto login(AuthRequestDto dto) {
        User user = findByIdentifier(dto.identifier());

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new BadCredentialsException("Credenciales incorrectas");
        }

        if (user.isTwoFactorEnabled()) {
            if (dto.totpCode() == null || dto.totpCode().isBlank()) {
                return AuthResponseDto.requireTotp();
            }
            verifyTotp(user.getTwoFactorSecret(), dto.totpCode());
        }

        return AuthResponseDto.success(jwtService.generateToken(user));
    }

    // =========================================================
    // ACTIVAR / DESACTIVAR 2FA
    // =========================================================

    /**
     * Activa 2FA para el usuario autenticado.
     * Devuelve la URL otpauth:// y la imagen QR del autenticador.
     */
    @Transactional
    public String enable2fa(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (user.isTwoFactorEnabled()) {
            throw new IllegalStateException("El 2FA ya está activado");
        }

        user.setTwoFactorEnabled(true);
        userRepository.save(user);

        // Devolvemos la URL otpauth:// para que el cliente la muestre como QR
        return buildOtpAuthUrl(user.getUsername(), user.getTwoFactorSecret());
    }

    /**
     * Desactiva 2FA para el usuario autenticado.
     */
    @Transactional
    public void disable2fa(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        user.setTwoFactorEnabled(false);
        userRepository.save(user);
    }

    // =========================================================
    // QR DE SESIÓN (login desde QR escaneado)
    // =========================================================

    /**
     * Genera un QR de sesión para que el usuario ya autenticado
     * lo escanee y obtenga un JWT en otro dispositivo/cliente.
     */
    @Transactional
    public QrLoginResponseDto generateQrSessionCode(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        String sessionToken = UUID.randomUUID().toString();
        user.setQrSessionToken(sessionToken);
        userRepository.save(user);

        String qrContent = "qr-login:" + sessionToken;
        String base64Png = generateQrPngBase64(qrContent, 250);

        return new QrLoginResponseDto(base64Png, sessionToken,
                "Escanea este QR para iniciar sesión");
    }

    /**
     * Intercambia el token QR escaneado por un JWT.
     * Una vez consumido, el token se invalida.
     */
    @Transactional
    public AuthResponseDto loginWithQrToken(String qrSessionToken) {
        User user = userRepository.findByQrSessionToken(qrSessionToken)
                .orElseThrow(() -> new BadCredentialsException("QR inválido o expirado"));

        // Invalidar el token tras el uso
        user.setQrSessionToken(null);
        userRepository.save(user);

        return AuthResponseDto.success(jwtService.generateToken(user));
    }

    // =========================================================
    // HELPERS PRIVADOS
    // =========================================================

    private User findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            throw new IllegalArgumentException("Se debe proporcionar username o email");
        }
        // Si contiene '@' es email
        if (identifier.contains("@")) {
            return userRepository.findByEmail(identifier)
                    .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));
        }
        return userRepository.findByUsername(identifier)
                .orElseThrow(() -> new BadCredentialsException("Credenciales incorrectas"));
    }

    private void verifyTotp(String secret, String code) {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator(HashingAlgorithm.SHA1);
        CodeVerifier verifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

        if (!verifier.isValidCode(secret, code)) {
            throw new BadCredentialsException("Código TOTP incorrecto");
        }
    }

    private String buildOtpAuthUrl(String username, String secret) {
        String issuer = URLEncoder.encode("SpringAPI", StandardCharsets.UTF_8);
        String account = URLEncoder.encode(username, StandardCharsets.UTF_8);
        return String.format(
                "otpauth://totp/%s:%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                issuer, account, secret, issuer
        );
    }

    private String generateQrPngBase64(String content, int size) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Error generando QR", e);
        }
    }

    private String generateVerificationCode() {
        return String.format("%06d", new java.util.Random().nextInt(999999));
    }
}

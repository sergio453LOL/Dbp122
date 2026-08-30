package io.github.lazheart.spring_api.user.controller;

import io.github.lazheart.spring_api.user.dto.PublicUserResponseDto;
import io.github.lazheart.spring_api.user.dto.UserRequestDto;
import io.github.lazheart.spring_api.user.dto.UserResponseDto;
import io.github.lazheart.spring_api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints de usuarios (todos requieren autenticación JWT).
 *
 * ADMIN:
 *   GET  /api/users            → lista completa (id, username, email, phone, role)
 *   DELETE /api/users/{id}     → eliminar usuario por id
 *
 * USER y ADMIN:
 *   GET  /api/users/public     → lista pública (username, email)
 *   GET  /api/users/me         → perfil propio
 *   PATCH /api/users/me        → actualizar username, email, phoneNumber propios
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Usuarios", description = "Gestión de usuarios")
public class UserController {

    private final UserService userService;

    // ---- ADMIN ----

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Listar todos los usuarios con datos completos")
    public ResponseEntity<List<UserResponseDto>> listAll() {
        return ResponseEntity.ok(userService.listAllUsers());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "[ADMIN] Eliminar usuario por ID")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- USER y ADMIN ----

    @GetMapping("/public")
    @Operation(summary = "Listar usuarios (username y email, sin ID)")
    public ResponseEntity<List<PublicUserResponseDto>> listPublic() {
        return ResponseEntity.ok(userService.listUsersPublic());
    }

    @GetMapping("/me")
    @Operation(summary = "Obtener perfil del usuario autenticado")
    public ResponseEntity<UserResponseDto> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getCurrentUser(userDetails.getUsername()));
    }

    @PatchMapping("/me")
    @Operation(summary = "Actualizar datos del usuario autenticado (username, email, phoneNumber)")
    public ResponseEntity<UserResponseDto> updateMe(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserRequestDto dto) {
        return ResponseEntity.ok(userService.updateCurrentUser(userDetails.getUsername(), dto));
    }
}

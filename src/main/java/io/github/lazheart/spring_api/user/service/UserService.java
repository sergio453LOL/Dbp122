package io.github.lazheart.spring_api.user.service;

import io.github.lazheart.spring_api.user.domain.User;
import io.github.lazheart.spring_api.user.dto.PublicUserResponseDto;
import io.github.lazheart.spring_api.user.dto.UserRequestDto;
import io.github.lazheart.spring_api.user.dto.UserResponseDto;
import io.github.lazheart.spring_api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Servicio de gestión de usuarios.
 * <ul>
 *   <li>ADMIN: listar todos (id, username, email, phoneNumber), eliminar por id.</li>
 *   <li>USER: ver username y email de otros usuarios, actualizar sus propios datos.</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // =========================================================
    // OPERACIONES ADMIN
    // =========================================================

    /** Lista todos los usuarios con datos completos (solo ADMIN). */
    public List<UserResponseDto> listAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toFullDto)
                .toList();
    }

    /** Elimina un usuario por su UUID (solo ADMIN). */
    @Transactional
    public void deleteUserById(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("Usuario no encontrado con id: " + id);
        }
        userRepository.deleteById(id);
    }

    // =========================================================
    // OPERACIONES USER
    // =========================================================

    /** Devuelve todos los usuarios con solo username y email (para usuarios con rol USER). */
    public List<PublicUserResponseDto> listUsersPublic() {
        return userRepository.findAll().stream()
                .map(u -> new PublicUserResponseDto(u.getUsername(), u.getEmail()))
                .toList();
    }

    /**
     * Actualiza los datos del usuario autenticado.
     * Solo se modifican los campos que no sean nulos en el DTO.
     */
    @Transactional
    public UserResponseDto updateCurrentUser(String username, UserRequestDto dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (dto.username() != null && !dto.username().isBlank()) {
            if (!dto.username().equals(user.getUsername())
                    && userRepository.existsByUsername(dto.username())) {
                throw new IllegalArgumentException("El username ya está en uso");
            }
            user.setUsername(dto.username());
        }

        if (dto.email() != null && !dto.email().isBlank()) {
            if (!dto.email().equals(user.getEmail())
                    && userRepository.existsByEmail(dto.email())) {
                throw new IllegalArgumentException("El email ya está en uso");
            }
            user.setEmail(dto.email());
        }

        if (dto.phoneNumber() != null && !dto.phoneNumber().isBlank()) {
            if (!dto.phoneNumber().equals(user.getPhoneNumber())
                    && userRepository.existsByPhoneNumber(dto.phoneNumber())) {
                throw new IllegalArgumentException("El número de teléfono ya está en uso");
            }
            user.setPhoneNumber(dto.phoneNumber());
        }

        return toFullDto(userRepository.save(user));
    }

    /** Devuelve el perfil del usuario autenticado. */
    public UserResponseDto getCurrentUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        return toFullDto(user);
    }

    // =========================================================
    // MAPPER
    // =========================================================

    private UserResponseDto toFullDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole()
        );
    }
}

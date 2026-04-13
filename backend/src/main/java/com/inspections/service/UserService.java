package com.inspections.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inspections.dto.AvatarUploadResponse;
import com.inspections.dto.UpdateProfileRequest;
import com.inspections.dto.UserProfileResponse;
import com.inspections.entity.User;
import com.inspections.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para operaciones de usuario (perfil).
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${server.port:8080}")
    private String serverPort;

    public UserService(UserRepository userRepository,
                       AuditService auditService,
                       ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.objectMapper = objectMapper;
    }

    /**
     * Obtiene el perfil de un usuario por su ID.
     *
     * @param id UUID del usuario
     * @return UserProfileResponse con los datos del perfil
     * @throws UsernameNotFoundException si no existe el usuario
     */
    public UserProfileResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con ID: " + id));

        return mapToProfileResponse(user);
    }

    /**
     * Lista todos los usuarios del sistema.
     *
     * @return Lista de UserProfileResponse
     */
    public List<UserProfileResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToProfileResponse)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza el rol de un usuario.
     * Solo permite INSPECTOR u OPERATOR. No se puede asignar ADMIN.
     *
     * @param userId  UUID del usuario
     * @param newRole Nuevo rol (INSPECTOR u OPERATOR)
     * @return UserProfileResponse con el perfil actualizado
     */
    public UserProfileResponse updateUserRole(String userId, String newRole, String callerEmail) {
        if ("ADMIN".equalsIgnoreCase(newRole)) {
            throw new IllegalArgumentException("No se puede asignar rol ADMIN");
        }
        String normalized = newRole != null ? newRole.toUpperCase() : null;
        if (!"INSPECTOR".equals(normalized) && !"OPERATOR".equals(normalized)) {
            throw new IllegalArgumentException("Rol inválido. Solo INSPECTOR u OPERATOR.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con ID: " + userId));
        String oldRole = user.getRole();
        String targetEmail = user.getEmail();
        user.setRole(normalized);
        UserProfileResponse saved = mapToProfileResponse(userRepository.save(user));
        String actor = callerEmail != null ? callerEmail.trim().toLowerCase() : "";
        Map<String, String> meta = new LinkedHashMap<>();
        meta.put("oldRole", oldRole);
        meta.put("newRole", normalized);
        meta.put("targetEmail", targetEmail);
        try {
            auditService.log(actor, "User", userId, "ROLE_CHANGE", objectMapper.writeValueAsString(meta));
        } catch (JsonProcessingException e) {
            auditService.log(actor, "User", userId, "ROLE_CHANGE", null);
        }
        return saved;
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getAvatarImage(),
                user.getRole()
        );
    }

    /**
     * Actualiza el perfil de un usuario.
     * Solo actualiza firstName, lastName, phoneNumber, avatarImage.
     *
     * @param userId  UUID del usuario
     * @param request Datos a actualizar
     * @return UserProfileResponse con el perfil actualizado
     */
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con ID: " + userId));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAvatarImage() != null) {
            user.setAvatarImage(request.getAvatarImage());
        }

        userRepository.save(user);
        return mapToProfileResponse(user);
    }

    /**
     * Sube el avatar de un usuario.
     * Guarda el archivo en uploads/avatars/{userId}.{ext} y actualiza la URL en la DB.
     *
     * @param userId ID del usuario
     * @param file   Archivo de imagen (JPEG o PNG)
     * @return AvatarUploadResponse con la URL pública del avatar
     * @throws IOException si hay error al guardar el archivo
     */
    public AvatarUploadResponse uploadAvatar(String userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con ID: " + userId));

        // Validar tipo MIME
        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            throw new IllegalArgumentException("Solo se permiten imágenes JPEG o PNG.");
        }

        // Determinar extensión
        String extension = contentType.equals("image/png") ? ".png" : ".jpg";

        // Crear directorio si no existe
        Path avatarsDir = Paths.get(uploadDir, "avatars").toAbsolutePath().normalize();
        Files.createDirectories(avatarsDir);

        // Guardar archivo
        String fileName = userId + extension;
        Path filePath = avatarsDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Construir URL relativa para servir
        String avatarUrl = "/uploads/avatars/" + fileName;

        // Actualizar usuario
        user.setAvatarImage(avatarUrl);
        userRepository.save(user);

        return new AvatarUploadResponse(avatarUrl);
    }
}

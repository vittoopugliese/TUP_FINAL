package com.inspections.controller;

import com.inspections.dto.AvatarUploadResponse;
import com.inspections.dto.UpdateProfileRequest;
import com.inspections.dto.UpdateRoleRequest;
import com.inspections.dto.UserProfileResponse;
import com.inspections.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.io.IOException;

/**
 * Endpoints de usuario (perfil).
 *
 * GET  /api/users/{id}        – Obtener perfil de usuario por ID
 * PUT  /api/users/{id}        – Actualizar perfil de usuario
 * POST /api/users/{id}/avatar – Subir avatar de usuario
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Operaciones de perfil de usuario")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Listar todos los usuarios",
               description = "Retorna la lista de usuarios del sistema (para admin)")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers() {
        List<UserProfileResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener perfil de usuario",
               description = "Retorna los datos del perfil de un usuario por su ID")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String id) {
        UserProfileResponse profile = userService.getUserById(id);
        return ResponseEntity.ok(profile);
    }

    @PatchMapping("/{id}/role")
    @Operation(summary = "Actualizar rol de usuario",
               description = "Actualiza el rol de un usuario. Solo INSPECTOR u OPERATOR.")
    public ResponseEntity<?> updateUserRole(
            @PathVariable String id,
            @Valid @RequestBody UpdateRoleRequest request) {
        try {
            UserProfileResponse profile = userService.updateUserRole(id, request.getRole());
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar perfil de usuario",
               description = "Actualiza firstName, lastName, phoneNumber y avatarImage del usuario")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @PathVariable String id,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserProfileResponse profile = userService.updateProfile(id, request);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/{id}/avatar")
    @Operation(summary = "Subir avatar de usuario",
               description = "Sube una imagen JPEG o PNG como avatar del usuario (máximo 5MB)")
    public ResponseEntity<?> uploadAvatar(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        try {
            AvatarUploadResponse response = userService.uploadAvatar(id, file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Error al guardar el archivo: " + e.getMessage());
        }
    }
}

package com.inspections.controller;

import com.inspections.dto.UserProfileResponse;
import com.inspections.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de usuario (perfil).
 *
 * GET /api/users/{id} – Obtener perfil de usuario por ID
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Operaciones de perfil de usuario")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener perfil de usuario",
               description = "Retorna los datos del perfil de un usuario por su ID")
    public ResponseEntity<UserProfileResponse> getUserProfile(@PathVariable String id) {
        UserProfileResponse profile = userService.getUserById(id);
        return ResponseEntity.ok(profile);
    }
}

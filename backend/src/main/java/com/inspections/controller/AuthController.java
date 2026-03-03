package com.inspections.controller;

import com.inspections.dto.AuthRequest;
import com.inspections.dto.AuthResponse;
import com.inspections.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de autenticación.
 *
 * POST /api/auth/login    – Login con email/contraseña → JWT
 * POST /api/auth/logout   – Logout (client-side, stateless)
 * POST /api/auth/refresh  – Renovar token
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Autenticación y gestión de tokens JWT")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica con email/contraseña y retorna un JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalida sesión (client-side: descartar el token)")
    public ResponseEntity<Void> logout() {
        // Backend stateless: el cliente simplemente descarta el token.
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Renueva el JWT con el token actual válido")
    public ResponseEntity<AuthResponse> refresh(
            @RequestHeader("Authorization") String authorizationHeader) {
        AuthResponse response = authService.refresh(authorizationHeader);
        return ResponseEntity.ok(response);
    }
}

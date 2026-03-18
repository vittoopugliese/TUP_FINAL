package com.inspections.controller;

import com.inspections.dto.AuthRequest;
import com.inspections.dto.AuthResponse;
import com.inspections.dto.ForgotPasswordRequest;
import com.inspections.dto.RegisterRequest;
import com.inspections.dto.ResetPasswordRequest;
import com.inspections.service.AuthService;
import com.inspections.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Endpoints de autenticación.
 *
 * POST /api/auth/login             – Login con email/contraseña → JWT
 * POST /api/auth/logout            – Logout (client-side, stateless)
 * POST /api/auth/refresh           – Renovar token
 * POST /api/auth/forgot-password   – Solicitar recuperación de contraseña
 * POST /api/auth/reset-password    – Restablecer contraseña con token
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Autenticación y gestión de tokens JWT")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService,
                          PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Autentica con email/contraseña y retorna un JWT")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @Operation(summary = "Registro", description = "Registra un nuevo usuario con nombre, email y contraseña")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        }
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

    @PostMapping("/forgot-password")
    @Operation(summary = "Solicitar recuperación de contraseña",
               description = "Genera un token de recuperación (15 min) y envía un email con el código")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.createPasswordResetToken(request.getEmail());
        // Respuesta genérica por seguridad (no revelar si el email existía)
        return ResponseEntity.ok(Map.of(
                "message", "Si el email está registrado, recibirás un correo con instrucciones para restablecer tu contraseña."
        ));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Restablecer contraseña",
               description = "Valida el token de recuperación y establece la nueva contraseña")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok(Map.of(
                    "message", "Contraseña restablecida exitosamente. Ya podés iniciar sesión con tu nueva contraseña."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}


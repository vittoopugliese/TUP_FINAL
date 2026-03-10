package com.inspections.service;

import com.inspections.dto.AuthRequest;
import com.inspections.dto.AuthResponse;
import com.inspections.dto.ForgotPasswordRequest;
import com.inspections.entity.User;
import com.inspections.repository.UserRepository;
import com.inspections.security.JwtUtil;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Servicio de autenticación.
 * Valida credenciales, genera JWT para login y refresh.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Login: valida email/contraseña y retorna JWT + datos del usuario.
     */
    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + request.getEmail()));

        if (!user.isEnabled()) {
            throw new BadCredentialsException("Cuenta deshabilitada");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Contraseña incorrecta");
        }

        // Actualizar lastLoginAt
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token, user.getEmail(), user.getRole(),
                user.getId(), user.getFullName());
    }

    /**
     * Refresh: valida el token actual y emite uno nuevo.
     */
    public AuthResponse refresh(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new BadCredentialsException("Token inválido o faltante");
        }
        String token = bearerToken.substring(7);

        if (!jwtUtil.validateToken(token)) {
            throw new BadCredentialsException("Token inválido o expirado");
        }

        String email = jwtUtil.getEmailFromToken(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        String newToken = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(newToken, user.getEmail(), user.getRole(),
                user.getId(), user.getFullName());
    }

    /**
     * Forgot password: verifica que el email esté registrado.
     * En producción enviaría un email con link de reset; aquí solo valida la
     * existencia.
     *
     * @param request contiene el email del usuario
     * @return mensaje de confirmación
     * @throws UsernameNotFoundException si el email no está registrado
     */
    public String forgotPassword(ForgotPasswordRequest request) {
        userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException(
                        "No se encontró una cuenta con el email: " + request.getEmail()));

        // En producción: generar token de reset y enviar email
        return "Se envió un enlace de recuperación al correo: " + request.getEmail();
    }
}

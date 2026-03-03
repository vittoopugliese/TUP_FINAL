package com.inspections.service;

import com.inspections.dto.AuthRequest;
import com.inspections.dto.AuthResponse;
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
        String token = bearerToken.startsWith("Bearer ")
                ? bearerToken.substring(7) : bearerToken;

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
}

package com.inspections.service;

import com.inspections.entity.PasswordResetToken;
import com.inspections.entity.User;
import com.inspections.repository.PasswordResetTokenRepository;
import com.inspections.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Servicio de restablecimiento de contraseña.
 *
 * Flujo:
 * 1. El usuario solicita reset → se genera un token UUID con 15 min de expiración
 * 2. Se envía email con el token
 * 3. El usuario envía el token + nueva contraseña → se valida y actualiza
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(UserRepository userRepository,
                                PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder,
                                EmailService emailService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Genera un token de recuperación y envía el email.
     * Si el email no existe, NO lanza excepción (seguridad: no revelar si existe o no).
     *
     * @param email email del usuario
     */
    @Transactional
    public void createPasswordResetToken(String email) {
        var userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            log.warn("Solicitud de reset para email inexistente: {}", email);
            // No revelamos si el email existe o no por seguridad
            return;
        }

        User user = userOpt.get();

        // Eliminar tokens previos del usuario
        tokenRepository.deleteByUser(user);

        // Generar nuevo token UUID
        String tokenValue = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(tokenValue, user);
        tokenRepository.save(resetToken);

        log.info("Token de recuperación generado para usuario: {} (expira en 15 min)", user.getEmail());

        // Enviar email
        emailService.sendPasswordResetEmail(user.getEmail(), tokenValue);
    }

    /**
     * Valida el token y restablece la contraseña.
     *
     * @param tokenValue  token UUID recibido del usuario
     * @param newPassword nueva contraseña en texto plano
     * @throws IllegalArgumentException si el token es inválido, expirado o ya fue usado
     */
    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken resetToken = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Token de recuperación inválido"));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Este token ya fue utilizado");
        }

        if (resetToken.isExpired()) {
            throw new IllegalArgumentException(
                    "El token de recuperación ha expirado (válido por 15 minutos)");
        }

        // Actualizar contraseña
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Marcar token como usado
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);

        log.info("✅ Contraseña restablecida exitosamente para: {}", user.getEmail());
    }
}

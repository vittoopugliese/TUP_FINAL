package com.inspections.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Servicio para envío de emails.
 * Envía el correo de recuperación de contraseña con un template HTML básico.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@inspections.com}")
    private String fromEmail;

    @Value("${app.password-reset.base-url:http://localhost:8080}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envía un email de recuperación de contraseña.
     *
     * @param toEmail email del usuario
     * @param token   token UUID de recuperación
     */
    public void sendPasswordResetEmail(String toEmail, String token) {
        String resetUrl = baseUrl + "/api/auth/reset-password?token=" + token;

        String htmlContent = """
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px;">
                <div style="max-width: 600px; margin: 0 auto; background-color: #ffffff;
                            padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);">
                    <h2 style="color: #333;">🔐 Recuperación de contraseña</h2>
                    <p style="color: #555;">Recibiste este email porque solicitaste restablecer tu contraseña
                       en la aplicación de <strong>Inspecciones</strong>.</p>
                    <p style="color: #555;">Tu código de recuperación es:</p>
                    <div style="background-color: #f0f0f0; padding: 15px; border-radius: 8px;
                                text-align: center; margin: 20px 0;">
                        <span style="font-size: 24px; font-weight: bold; letter-spacing: 2px; color: #1a73e8;">
                            %s
                        </span>
                    </div>
                    <p style="color: #555;">Este código expira en <strong>15 minutos</strong>.</p>
                    <p style="color: #555;">También podés usar el siguiente enlace para restablecer tu contraseña:</p>
                    <p><a href="%s" style="color: #1a73e8;">%s</a></p>
                    <hr style="border: none; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="color: #999; font-size: 12px;">Si no solicitaste este cambio, ignorá este mensaje.
                       Tu contraseña no será modificada.</p>
                </div>
            </body>
            </html>
            """.formatted(token, resetUrl, resetUrl);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Recuperación de contraseña - Inspecciones");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("✅ Email de recuperación enviado a: {}", toEmail);

        } catch (MessagingException | MailException e) {
            log.error("❌ Error al enviar email de recuperación a {}: {}", toEmail, e.getMessage());
            // En desarrollo, logueamos el token para poder testearlo manualmente
            log.info("🔑 Token de recuperación para {}: {}", toEmail, token);
        }
    }
}

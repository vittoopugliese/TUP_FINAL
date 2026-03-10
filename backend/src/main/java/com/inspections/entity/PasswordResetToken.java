package com.inspections.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Token de restablecimiento de contraseña.
 * Se genera un UUID aleatorio con una expiración de 15 minutos.
 * Una vez usado, se marca como used=true para evitar reutilización.
 */
@Entity
@Table(name = "password_reset_tokens", indexes = {
    @Index(name = "idx_prt_token", columnList = "token", unique = true)
})
public class PasswordResetToken {

    private static final int EXPIRATION_MINUTES = 15;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean used = false;

    @Column(nullable = false)
    private Instant createdAt;

    public PasswordResetToken() {}

    /**
     * Crea un token de reset para el usuario dado.
     * @param token  UUID generado externamente
     * @param user   usuario que solicita el reset
     */
    public PasswordResetToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.createdAt = Instant.now();
        this.expiryDate = this.createdAt.plus(EXPIRATION_MINUTES, ChronoUnit.MINUTES);
    }

    /** @return true si la fecha actual supera la expiración */
    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }

    // ── Getters / Setters ──────────────────────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }

    public boolean isUsed() { return used; }
    public void setUsed(boolean used) { this.used = used; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}

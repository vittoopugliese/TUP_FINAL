package com.example.tup_final.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room para User.
 * Representa un usuario con credenciales de autenticación y perfil.
 * Roles de usuario: ADMIN, SUPERVISOR, INSPECTOR.
 */

@Entity(
    tableName = "users",
    indices = { @Index("email") }
)

public class UserEntity {
    @PrimaryKey
    @NonNull
    public String id;
    public String email;
    public String firstName;
    public String lastName;
    /** URL de la imagen de perfil. */
    public String avatarImage;

    /** Número de teléfono. */
    public String phoneNumber;

    /** Rol: ADMIN, SUPERVISOR o INSPECTOR. */
    public String role;

    /** Marca de tiempo de último login (ISO string). */
    public String lastLoginAt;

    /** Marca de tiempo de creación (ISO string). */
    public String createdAt;

    public UserEntity() {
    }

    public UserEntity(String id, String email, String firstName, String lastName, String avatarImage, String phoneNumber, String role, String lastLoginAt, String createdAt) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarImage = avatarImage;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.lastLoginAt = lastLoginAt;
        this.createdAt = createdAt;
    }

    public String getFullName() {
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "").trim();
    }
}
package com.example.tup_final.data.remote.dto;

/**
 * DTO de respuesta para la subida de avatar.
 * Coincide con AvatarUploadResponse del backend.
 */
public class AvatarUploadResponse {

    private String avatarUrl;

    public AvatarUploadResponse() {}

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}

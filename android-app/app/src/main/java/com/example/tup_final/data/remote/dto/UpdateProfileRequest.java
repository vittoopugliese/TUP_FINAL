package com.example.tup_final.data.remote.dto;

/**
 * DTO para actualizar el perfil de usuario.
 * Coincide con UpdateProfileRequest del backend.
 * Campos editables: firstName, lastName, phoneNumber, avatarImage.
 */
public class UpdateProfileRequest {

    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String avatarImage;

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String firstName, String lastName, String phoneNumber, String avatarImage) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.avatarImage = avatarImage;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAvatarImage() { return avatarImage; }
    public void setAvatarImage(String avatarImage) { this.avatarImage = avatarImage; }
}

package com.inspections.dto;

import jakarta.validation.constraints.Size;

/**
 * DTO para actualizar el perfil de usuario.
 * Solo campos editables: firstName, lastName, phoneNumber, avatarImage.
 * email y role NO son editables por este endpoint.
 */
public class UpdateProfileRequest {

    @Size(max = 255)
    private String firstName;

    @Size(max = 255)
    private String lastName;

    @Size(max = 50)
    private String phoneNumber;

    @Size(max = 500)
    private String avatarImage;

    public UpdateProfileRequest() {}

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAvatarImage() { return avatarImage; }
    public void setAvatarImage(String avatarImage) { this.avatarImage = avatarImage; }
}

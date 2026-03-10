package com.inspections.dto;

/**
 * DTO de respuesta para el perfil de usuario.
 * Excluye campos sensibles como passwordHash.
 */
public class UserProfileResponse {

    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String avatarImage;
    private String role;

    public UserProfileResponse() {}

    public UserProfileResponse(String id, String email, String firstName, String lastName,
                               String phoneNumber, String avatarImage, String role) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.avatarImage = avatarImage;
        this.role = role;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getAvatarImage() { return avatarImage; }
    public void setAvatarImage(String avatarImage) { this.avatarImage = avatarImage; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}

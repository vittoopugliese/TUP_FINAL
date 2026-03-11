package com.inspections.service;

import com.inspections.dto.UpdateProfileRequest;
import com.inspections.dto.UserProfileResponse;
import com.inspections.entity.User;
import com.inspections.repository.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Servicio para operaciones de usuario (perfil).
 */
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Obtiene el perfil de un usuario por su ID.
     *
     * @param id UUID del usuario
     * @return UserProfileResponse con los datos del perfil
     * @throws UsernameNotFoundException si no existe el usuario
     */
    public UserProfileResponse getUserById(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con ID: " + id));

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getAvatarImage(),
                user.getRole()
        );
    }

    /**
     * Actualiza el perfil de un usuario.
     * Solo actualiza firstName, lastName, phoneNumber, avatarImage.
     *
     * @param userId  UUID del usuario
     * @param request Datos a actualizar
     * @return UserProfileResponse con el perfil actualizado
     */
    public UserProfileResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado con ID: " + userId));

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAvatarImage() != null) {
            user.setAvatarImage(request.getAvatarImage());
        }

        userRepository.save(user);

        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getAvatarImage(),
                user.getRole()
        );
    }
}

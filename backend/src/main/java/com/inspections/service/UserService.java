package com.inspections.service;

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
}

package com.inspections.config;

import com.inspections.entity.User;
import com.inspections.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

/**
 * Inicializador de datos para desarrollo.
 * Crea usuarios de prueba por rol (admin, inspector, operador).
 * Las inspecciones y demás datos vienen de data.sql.
 *
 * Credenciales de prueba:
 * - ADMIN:      admin@inspections.com      / Admin1234!
 * - INSPECTOR:  inspector@example.com    / Inspector123 (asignado a todas las inspecciones en data.sql)
 * - OPERATOR 1: operador@inspections.com / Operador123
 * - OPERATOR 2: operador1@inspections.com / Operador123
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    public static final String ADMIN_USER_ID = "admin-001";
    public static final String OPERATOR_USER_ID = "operator-001";
    public static final String INSPECTOR_USER_ID = "inspector-001";
    public static final String OPERATOR2_USER_ID = "operator-002";

    @Bean
    public CommandLineRunner seedData(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            createUserIfNotExists(userRepository, passwordEncoder,
                    ADMIN_USER_ID, "admin@inspections.com", "Admin1234!",
                    "Admin", "Inspector", "ADMIN");
            createUserIfNotExists(userRepository, passwordEncoder,
                    OPERATOR_USER_ID, "operador@inspections.com", "Operador123",
                    "Operador", "Sistema", "OPERATOR");
            createUserIfNotExists(userRepository, passwordEncoder,
                    INSPECTOR_USER_ID, "inspector@example.com", "Inspector123",
                    "María", "García", "INSPECTOR");
            createUserIfNotExists(userRepository, passwordEncoder,
                    OPERATOR2_USER_ID, "operador1@inspections.com", "Operador123",
                    "Operador", "Uno", "OPERATOR");
        };
    }

    private void createUserIfNotExists(UserRepository repo, PasswordEncoder encoder,
                                      String id, String email, String password,
                                      String firstName, String lastName, String role) {
        if (repo.findByEmailIgnoreCase(email).isEmpty()) {
            User user = new User();
            user.setId(id);
            user.setEmail(email);
            user.setPasswordHash(encoder.encode(password));
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setRole(role);
            user.setEnabled(true);
            user.setCreatedAt(Instant.now());
            repo.save(user);
            log.info("✅ Usuario creado: {} / {} (rol: {})", email, password, role);
        }
    }
}

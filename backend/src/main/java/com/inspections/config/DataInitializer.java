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
 * Crea el usuario admin de prueba (admin@inspections.com).
 * Las inspecciones y demás datos vienen de data.sql (database/data-ejemplo.sql).
 *
 * Credenciales:  admin@inspections.com / Admin1234!
 */
@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    /** ID fijo para que audit_logs en data.sql puedan referenciarlo */
    public static final String ADMIN_USER_ID = "admin-001";

    @Bean
    public CommandLineRunner seedData(UserRepository userRepository,
                                     PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setId(ADMIN_USER_ID);
                admin.setEmail("admin@inspections.com");
                admin.setPasswordHash(passwordEncoder.encode("Admin1234!"));
                admin.setFirstName("Admin");
                admin.setLastName("Inspector");
                admin.setRole("INSPECTOR");
                admin.setEnabled(true);
                admin.setCreatedAt(Instant.now());

                userRepository.save(admin);
                log.info("✅ Usuario admin creado: admin@inspections.com / Admin1234!");
            } else {
                log.info("✅ Base de datos ya inicializada.");
            }
        };
    }
}

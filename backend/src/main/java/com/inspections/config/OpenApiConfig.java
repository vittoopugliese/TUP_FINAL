package com.inspections.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Swagger / OpenAPI 3.
 * UI disponible en: http://localhost:8080/swagger-ui.html
 * Docs JSON en:     http://localhost:8080/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inspectionsOpenAPI() {
        final String jwtSchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Inspections API")
                        .description("API REST para el sistema de inspección digital de dispositivos contra incendios")
                        .version("v0.1.0")
                        .contact(new Contact()
                                .name("TUP Final")
                                .email("inspections@example.com")))
                .addSecurityItem(new SecurityRequirement().addList(jwtSchemeName))
                .components(new Components()
                        .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                                .name(jwtSchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresá el token JWT (sin el prefijo 'Bearer ')")));
    }
}

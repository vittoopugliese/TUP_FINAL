# Backend - Inspections API

API REST para el sistema de inspección digital de dispositivos contra incendios. Servirá como servidor central para autenticación, sincronización de datos y gestión de inspecciones.

## Tecnología

- **Framework:** Spring Boot (Java)
- **Build:** Maven o Gradle
- **Base de datos:** JPA/Hibernate con schema definido en `../database/schema.sql`
- **Seguridad:** Spring Security + JWT
- **Documentación API:** Swagger/OpenAPI

## Estructura Prevista

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/inspections/
│   │   │       ├── config/          # Configuración, Security
│   │   │       ├── controller/      # REST Controllers
│   │   │       ├── service/         # Lógica de negocio
│   │   │       ├── repository/      # JPA Repositories
│   │   │       ├── entity/          # Entidades JPA
│   │   │       ├── dto/             # Request/Response DTOs
│   │   │       └── security/        # JWT, filtros
│   │   └── resources/
│   │       ├── application.properties
│   │       └── application-dev.properties
│   └── test/
├── pom.xml (o build.gradle)
└── README.md
```

## Tareas a Implementar (según planning)

### Fase 1 - Base
- Inicializar proyecto Spring Boot (Java 17+, Maven/Gradle)
- Configurar JPA/Hibernate con schema de `database/schema.sql`
- Implementar endpoints de autenticación:
  - `POST /api/auth/login` - Login con email/contraseña, retorna JWT
  - `POST /api/auth/logout` - Invalidar sesión
  - `POST /api/auth/refresh` - Renovar token
- Configurar Spring Security con JWT
- Documentar API con Swagger/OpenAPI

### Fase 2 - Autenticación extendida
- `POST /api/auth/forgot-password` - Solicitar recuperación (token 15 min, envío email)
- `POST /api/auth/reset-password` - Restablecer con token
- Política de contraseña: 8 chars, mayúscula, minúscula, número, especial
- Rate limiting: máx 3 solicitudes recuperación cada 30 min por IP/email

### Fase 3 - Inspecciones y sync
- Endpoints CRUD para inspecciones, locations, zones, devices, tests, steps
- Endpoint de sincronización para la app offline-first
- Gestión de conflictos con versionado
- Registro de auditoría (fecha/hora/usuario)

### Fase 4 - Reportes
- Endpoint para subir PDFs generados por la app
- Almacenamiento de reportes en servidor

## Requisitos Previos

- Java 17 o superior
- PostgreSQL o H2 para desarrollo
- (Opcional) SMTP configurado para emails de recuperación de contraseña

## Comandos de Ejecución (cuando esté implementado)

```bash
# Con Maven
./mvnw spring-boot:run

# Con Gradle
./gradlew bootRun
```

## Referencias

- Schema de base de datos: `../database/schema.sql`
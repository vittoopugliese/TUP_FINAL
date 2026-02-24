# Inspections - Sistema de Inspección Digital de Dispositivos contra Incendios

Aplicación móvil Android para la digitalización completa de inspecciones técnicas de sistemas contra incendios en edificios y plantas industriales.

## Estructura del Proyecto (Monorepo)

```
TUP_FINAL/
├── android-app/          # Aplicación Android nativa
├── backend/              # API REST (Spring Boot) - por implementar
├── database/             # Scripts SQL - por implementar
├── docs/                 # Documentación - por implementar
└── README.md
```

---

## Android App - Setup

### Requisitos

- **Android Studio:** Hedgehog (2023.1.1) o superior
- **JDK:** 11
- **Android SDK:** API 26 (min) - API 36 (target)
- **Dispositivo/Emulador:** Android 8.0 (API 26) o superior

### Pasos para ejecutar

1. Abrir el proyecto en Android Studio:
   - `File` → `Open` → seleccionar la carpeta `android-app`

2. Sincronizar Gradle:
   - Android Studio detectará el proyecto y sincronizará automáticamente
   - O: `File` → `Sync Project with Gradle Files`

3. Ejecutar la app:
   - Conectar un dispositivo o iniciar un emulador
   - `Run` → `Run 'app'` (o Shift+F10)

### Build desde línea de comandos

```bash
cd android-app
./gradlew assembleDebug    # APK de debug
./gradlew assembleRelease  # APK de release (requiere firma configurada)
```

### Arquitectura Android

- **MVVM** con Clean Architecture
- **Hilt** para inyección de dependencias
- **Room** para persistencia local (SQLite)
- **Retrofit + OkHttp** para networking
- **Navigation Component** para navegación
- **ViewModel + LiveData** para estado de UI

### Estructura de paquetes

```
com.example.tup_final/
├── data/       # Repositorios, fuentes de datos, entidades
├── domain/     # Casos de uso, lógica de negocio
├── di/         # Módulos Hilt
├── ui/         # Activities, Fragments, ViewModels
└── util/       # Utilidades, constantes
```

---

## Backend - Setup (por implementar)

El backend será una API REST con Spring Boot.

### Requisitos previstos

- Java 17+
- Maven o Gradle
- PostgreSQL o H2 para desarrollo

### Pasos (cuando esté implementado)

```bash
cd backend
./mvnw spring-boot:run
# o
./gradlew bootRun
```

---

## Base de Datos - Setup (por implementar)

Los scripts SQL estarán en la carpeta `database/`.

### Archivos previstos

- `schema.sql` - DDL para crear tablas
- `data-ejemplo.sql` - Datos de prueba

---

## Documentación

La carpeta `docs/` contendrá:

- Manual de usuario (inspector y operador)
- Documentación técnica de arquitectura
- Guía de configuración de plantillas de test
- Plan de pruebas y casos de test

---

## Licencia

Proyecto académico - TUP Final.

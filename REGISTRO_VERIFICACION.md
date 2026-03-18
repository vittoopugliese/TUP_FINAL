# Verificación del flujo de registro

## Resumen de cambios

### Backend
- **POST /api/auth/register**: Nuevo endpoint que recibe `{ email, fullName, role, password }`
- Roles permitidos: `INSPECTOR`, `OPERATOR`
- Validación: email único, contraseña mínima 6 caracteres
- El usuario admin por defecto (`admin@inspections.com` / `Admin1234!`) se mantiene

### Android
- Enlace "Registrar nuevo usuario" debajo de "¿Olvidaste tu contraseña?" en la pantalla de login
- Nueva pantalla de registro con: email, nombre, rol (dropdown Inspector/Operador), contraseña, confirmar contraseña
- Tras registro exitoso, vuelve al login con el email precargado

## Pasos para verificar

1. **Iniciar backend**: `cd backend && mvn spring-boot:run`
2. **Iniciar app Android**: Ejecutar en emulador o dispositivo (base URL: `http://10.0.2.2:8080/api/` para emulador)

3. **Registrar un INSPECTOR**:
   - En login, tocar "Registrar nuevo usuario"
   - Email: `inspector@test.com`
   - Nombre: `Inspector Test`
   - Rol: seleccionar "Inspector"
   - Contraseña: `Test1234`
   - Confirmar contraseña: `Test1234`
   - Aceptar
   - Verificar que vuelve al login con el email precargado
   - Iniciar sesión con esas credenciales

4. **Registrar un OPERATOR**:
   - Repetir con email `operator@test.com`, rol "Operador"

5. **Email duplicado**:
   - Intentar registrar de nuevo con `inspector@test.com`
   - Verificar mensaje: "Ya existe una cuenta con ese correo electrónico"

6. **Admin por defecto**:
   - Cerrar sesión y volver a login
   - Iniciar sesión con `admin@inspections.com` / `Admin1234!`
   - Verificar que funciona

7. **Persistencia**:
   - Los usuarios persisten mientras el backend esté activo
   - Al reiniciar el backend (H2 en memoria), se pierden los usuarios nuevos; el admin se vuelve a crear automáticamente

# Tutorial: Iniciar la App Inspections

Guía paso a paso para poner en marcha la aplicación Android Inspections con su backend Spring Boot.

---

## Requisitos previos

| Componente | Requisito |
|------------|-----------|
| **Java** | JDK 17 (para backend) y JDK 11 (para Android, o usar el que trae Android Studio) |
| **Android Studio** | Hedgehog (2023.1.1) o superior |
| **Maven** | Incluido en el proyecto (`mvnw`) |
| **Dispositivo o emulador** | Android 8.0 (API 26) o superior |

---

## Paso 1: Iniciar el backend

El backend debe estar corriendo antes de usar la app, ya que el login se conecta a la API.

### Opción A: Desde terminal (Windows)

1. Abre **PowerShell** o **CMD**.
2. Navega a la carpeta del backend:
   ```powershell
   cd C:\Users\torib\Desktop\Program\TUP_FINAL\backend
   ```
3. Ejecuta:
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
4. Espera hasta ver algo como:
   ```
   Started InspectionsApplication in X.XXX seconds
   ```

### Opción B: Desde terminal (Linux/Mac)

```bash
cd /ruta/a/TUP_FINAL/backend
./mvnw spring-boot:run
```

### Opción C: Desde Android Studio / IntelliJ

1. Abre el proyecto `TUP_FINAL` en Android Studio.
2. En el panel **Project**, navega a `backend` → `src/main/java` → `com.inspections.InspectionsApplication`.
3. Clic derecho en `InspectionsApplication` → **Run 'InspectionsApplication'**.

### Verificar que el backend está corriendo

- Abre en el navegador: **http://localhost:8080/swagger-ui.html**
- Deberías ver la documentación Swagger de la API.
- El backend usa H2 en memoria y crea un usuario de prueba al iniciar.

---

## Paso 2: Iniciar la app Android

### Opción A: Desde Android Studio (recomendado)

1. Abre **Android Studio**.
2. **File** → **Open** → selecciona la carpeta `android-app` (no la raíz del proyecto).
3. Espera a que Gradle sincronice (barra de progreso abajo).
4. Conecta un dispositivo físico o inicia un emulador:
   - **Device Manager** (icono de teléfono) → **Create Device** si no tienes.
   - Ejecuta el emulador con **▶**.
5. Pulsa **Run** (▶️) o Shift+F10 para ejecutar la app.

### Opción B: Desde línea de comandos

```powershell
cd C:\Users\torib\Desktop\Program\TUP_FINAL\android-app
.\gradlew.bat assembleDebug
```

Luego instala el APK en el dispositivo/emulador:

```powershell
.\gradlew.bat installDebug
```

O desde Android Studio: **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**.

---

## Paso 3: Probar el login

1. La app se abre en la pantalla de **Login**.
2. Usa estas credenciales (creadas automáticamente por el backend):

   | Campo | Valor |
   |-------|-------|
   | **Email** | `admin@inspections.com` |
   | **Contraseña** | `Admin1234!` |

3. Pulsa **Iniciar sesión**.
4. Si todo funciona, verás la pantalla **Home** con el botón de logout.

---

## Resolución de problemas

### "Error de red" o "Login failed"

- **El backend debe estar corriendo** antes de iniciar la app.
- **Emulador:** La app usa `10.0.2.2:8080`, que es el host de tu PC. Si el backend no está en la misma máquina, no funcionará.
- **Dispositivo físico:** Hay que cambiar la IP en el código:
  - En `AppModule.java` y `SyncWorker.java` sustituye `10.0.2.2` por la IP de tu PC (ej: `192.168.1.10`).
  - En Windows: `ipconfig` para ver tu IP.

### Error "JAVA_HOME is not set"

- Instala JDK 17 y configura la variable de entorno:
  - Windows: Panel de control → Sistema → Variables de entorno → Nueva → `JAVA_HOME` = ruta a donde instalaste el JDK (ej: `C:\Program Files\Java\jdk-17`).
  - Linux/Mac: `export JAVA_HOME=/ruta/a/jdk-17`

### El backend no arranca

- Comprueba que el puerto 8080 no esté usado por otro programa.
- Ejecuta `.\mvnw.cmd clean spring-boot:run` para limpiar y reiniciar.

### La app no compila

- **File** → **Sync Project with Gradle Files**.
- **Build** → **Clean Project** y luego **Rebuild Project**.
- Verifica que usas JDK 11 en Android Studio: **File** → **Project Structure** → **SDK Location**.

---

## Flujo de ejecución

```
┌─────────────────┐     HTTP/API      ┌─────────────────┐
│   Backend       │     :8080         │   App Android   │
│   Spring Boot   │ ◄──────────────►  │   (Inspections) │
│   (H2 en RAM)   │                   │   Emulador/     │
└─────────────────┘                   │   Dispositivo   │
                                      └─────────────────┘
        ▲                                      │
        │                                      │ 10.0.2.2:8080
        │ localhost:8080                       │ (emulador)
        │                                      │
   ┌────┴────┐                           ┌─────┴─────┐
   │   PC    │                           │   App     │
   └─────────┘                           └───────────┘
```

---

## Resumen rápido

1. **Backend:** `cd backend` → `.\mvnw.cmd spring-boot:run`
2. **App:** Abrir `android-app` en Android Studio → Run
3. **Login:** `admin@inspections.com` / `Admin1234!`

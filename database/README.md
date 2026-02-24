# Database - Scripts SQL

Scripts SQL para la base de datos del sistema de inspección. Sirven como referencia para el backend (Spring Boot/JPA) y para la app Android (Room).

## Archivos a Crear

| Archivo | Descripción |
|---------|-------------|
| `schema.sql` | DDL para crear todas las tablas |
| `data-ejemplo.sql` | Datos de prueba para desarrollo |

## Modelo de Datos (según planning)

### Jerarquía de inspección (6 niveles)

```
Inspection (evento principal, vinculado a edificio/planta)
  └── Location (ubicación física: piso, sector, área)
        └── Zone (zona lógica: sala de máquinas, oficinas, depósito)
              └── Device (dispositivo: extintor, detector, rociador, etc.)
                    └── Test (verificación específica por tipo de dispositivo)
                          └── Step (pasos de verificación individuales)
```

### Entidades principales

- **users** - Usuarios (Operator, Inspector, roles)
- **buildings** - Edificios/plantas
- **inspections** - Inspecciones (estados: PENDING, IN_PROGRESS, DONE_FAILED, DONE_COMPLETED)
- **locations** - Ubicaciones físicas
- **zones** - Zonas lógicas
- **devices** - Dispositivos (tipo, modelo, fabricante)
- **device_types** - Catálogo de tipos (extintor, detector, rociador, etc.)
- **tests** - Tests predefinidos por tipo de dispositivo (estados: PENDING, COMPLETED, FAILED)
- **steps** - Pasos de verificación (estados: PENDING, SUCCESS, FAILED)
- **step_values** - Valores ingresados por step (binary, date_range, simple_value, numeric_range, multi_value)
- **observations** - Observaciones y deficiencias
- **photos** - Fotografías con metadatos (timestamp, GPS, inspector)
- **audit_log** - Registro de auditoría (fecha/hora/usuario)

### Estados de inspección

- `PENDING` - Pendiente de iniciar
- `IN_PROGRESS` - En curso
- `DONE_FAILED` - Finalizada con fallos (algún test FAILED)
- `DONE_COMPLETED` - Finalizada correctamente (todos los tests COMPLETED)

### Estados de test

- `PENDING`, `COMPLETED`, `FAILED`

### Estados de step

- `PENDING`, `SUCCESS`, `FAILED`

## Tareas a Implementar (según planning)

### Epic 1.1 - Base de datos local (Room en Android)
- Definir entidades: User, Inspection, Location, Zone, Device, Test, Step, Observation, Photo, AuditLog
- Crear DAOs para cada entidad
- Usar `schema.sql` como referencia para el modelo Room

### Epic 1.2 - Scripts SQL
- Crear `schema.sql` con DDL completo
- Crear `data-ejemplo.sql` con datos de prueba (usuarios, edificios, inspecciones, dispositivos, etc.)

## Uso

### Backend (Spring Boot)
- Importar `schema.sql` en la configuración inicial de la base de datos
- JPA/Hibernate puede generar el schema desde entidades, pero `schema.sql` sirve como referencia

### Android (Room)
- Las entidades Room deben reflejar el schema de `schema.sql`
- Mantener compatibilidad para sincronización con el backend
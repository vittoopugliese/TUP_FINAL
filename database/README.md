# Database - Scripts SQL

Scripts SQL para la base de datos del sistema de inspección. Sirven como referencia para el backend (Spring Boot/JPA) y para la app Android (Room).

## Archivos

| Archivo | Descripción |
|---------|-------------|
| `schema.sql` | DDL para crear todas las tablas (incluye locationId en inspections, inspection_assignments) |
| `data-ejemplo.sql` | Datos de prueba ricos para desarrollo (8 locations, 9 devices, 7 inspections, assignments) |

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
- **steps** - Pasos de verificación (estados: PENDING, COMPLETED, FAILED). Valores en `valueJson` + `minValue`/`maxValue` para rangos numéricos.
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

- `PENDING` - Sin valor o incompleto
- `COMPLETED` - Valor válido ingresado (legacy: `SUCCESS` se mapea a COMPLETED)
- `FAILED` - Validación fallida o deficiencia

### Tipos de step (testStepType) y contrato valueJson

| Tipo | Descripción | valueJson |
|------|-------------|-----------|
| BINARY | Sí/No dropdown | `{"value": true\|false\|null, "valueType": "BOOLEAN_VALUE"}` |
| DATE_RANGE | Dos fechas (desde/hasta) | `{"from": "ISO_DATE", "to": "ISO_DATE", "valueType": "DATE_RANGE_VALUE"}` |
| SIMPLE_VALUE | Texto, número o fecha simple | `{"value": "...", "valueType": "STRING_VALUE"\|"NUMERIC_VALUE"\|"DATE_VALUE"}` |
| NUMERIC_RANGE | Valor numérico con min/max | `{"value": number, "valueType": "NUMERIC_VALUE"}` + minValue/maxValue en columnas |
| MULTI_VALUE | Múltiples subcampos | `{"values": [{"name":"...","value":"...","valueType":"..."}]}` |
| RANGE | Legacy numérico | Mapear a NUMERIC_RANGE |

N/A por step: `applicable=false`. Excluido de validación y resultado.

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
- El backend usa `backend/src/main/resources/schema.sql` y `data.sql`
- `data.sql` está adaptado de `data-ejemplo.sql` para H2 (snake_case, formato de fechas)
- Los usuarios de prueba se crean en `backend/.../DataInitializer.java` (no en `data.sql`): admin, inspector y dos operadores — ver credenciales en el Javadoc de esa clase (`admin@inspections.com`, `inspector@example.com`, `operador@inspections.com`, `operador1@inspections.com`).
- Al iniciar el backend, se cargan automáticamente: locations, zones, devices, inspections, photos, tests, steps, observations, audit_logs

### Android (Room)
- Las entidades Room deben reflejar el schema de `schema.sql`
- Mantener compatibilidad para sincronización con el backend
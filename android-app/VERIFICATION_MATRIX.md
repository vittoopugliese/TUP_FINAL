# Matriz de verificación - Flujo Inspection Locations

## Escenarios de prueba

### Escenario 1: Inspección seed con buildingId que tiene locations
- **Precondición**: Backend corriendo con data.sql cargado. App Android conectada.
- **Pasos**: Login -> Home -> Tocar inspección (ej. insp-001, bld-001) -> Start/Continue -> Locations
- **Esperado**: Se ven cards de locations (Sala emergencias, Sala bombas, etc.) con nombre, descripción y contador de tests.
- **Causa si falla**: API de locations no responde, buildingId vacío, o sync falló.

### Escenario 2: Inspección visible pero sin locations en Room (offline)
- **Precondición**: App sin conexión o API caída. Room tiene inspecciones pero tabla locations vacía.
- **Pasos**: Login -> Home -> Tocar inspección -> Start/Continue -> Locations
- **Esperado**: Empty state "No hay ubicaciones en este edificio".
- **Causa**: Normal cuando no hay sync previo y no hay conexión.

### Escenario 3: Inspección PENDING sin inspector
- **Precondición**: Inspección en PENDING sin asignaciones de tipo INSPECTOR.
- **Pasos**: Home -> Tocar inspección -> Ver botón
- **Esperado**: Botón "Continuar" deshabilitado. Al tocar, mensaje de validación.
- **Causa si falla**: Lógica de isStartButtonEnabled o shouldShowStartLabel incorrecta.

### Escenario 4: Inspección IN_PROGRESS
- **Precondición**: Inspección ya en IN_PROGRESS (ej. insp-001, insp-006).
- **Pasos**: Home -> Tocar inspección -> Continuar -> Locations
- **Esperado**: Navega directo a locations sin cambiar estado. Lista de locations del building.
- **Causa si falla**: startOrContinueInspection no detecta IN_PROGRESS correctamente.

### Escenario 5: Crear nueva location desde InspectionLocations
- **Precondición**: En pantalla de locations de un building (ej. bld-001).
- **Pasos**: Nueva Ubicación -> Nombre "Sala prueba" -> Guardar -> Volver
- **Esperado**: Vuelve a la lista y la nueva location aparece en las cards.
- **Causa si falla**: createLocation no recibe buildingId o no lo persiste.

### Escenario 6: Create exitoso pero location no visible
- **Precondición**: Crear location desde InspectionLocations con buildingId válido.
- **Pasos**: Crear -> Volver
- **Esperado**: La location aparece en la lista filtrada por building.
- **Causa si falla**: buildingId = null en create, o filtro por buildingId no coincide.

## Checklist rápido

- [ ] Backend GET /api/locations?buildingId=bld-001 retorna locations
- [ ] Android sync locations al abrir InspectionLocations
- [ ] Cards muestran nombre, descripción, tests count
- [ ] Create location pasa buildingId y la nueva location aparece al volver
- [ ] Botón Start habilitado solo con PENDING + inspector
- [ ] Botón Continue habilitado para IN_PROGRESS

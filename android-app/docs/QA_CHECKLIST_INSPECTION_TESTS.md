# QA Checklist: Vista Zonas -> Devices -> Tests

## Flujo de navegación
- [ ] Login -> Home -> Inspección -> Start/Continue -> Locations
- [ ] Tap en una location navega a InspectionTestsFragment
- [ ] Toolbar muestra el nombre de la location seleccionada
- [ ] Botón back vuelve a la lista de locations

## Vista de Zonas/Devices/Tests
- [ ] Zonas se cargan y muestran correctamente
- [ ] Tap en zona expande/colapsa y muestra devices
- [ ] Tap en device expande/colapsa y muestra tests
- [ ] Ícono de expand/collapse rota correctamente (180° cuando expandido)
- [ ] Contador de dispositivos por zona es correcto
- [ ] Resumen "Completed X of Y tests" por device es correcto
- [ ] Indicador de estado (verde/naranja) por test según status

## Casos vacíos
- [ ] Location sin zonas: muestra mensaje "No hay zonas ni dispositivos en esta ubicación"
- [ ] Zona sin devices: muestra zona con "0 dispositivos"
- [ ] Device sin tests: muestra device con "Completed 0 of 0 tests"

## Estados de carga y error
- [ ] Loading: ProgressBar visible, lista oculta
- [ ] Error de red: Toast + mensaje en textEmpty, fallback a cache Room si existe
- [ ] Backend no disponible: fallback a Room cache sin crash

## Navegación a Steps
- [ ] Tap en test navega a StepsFragment
- [ ] Argumentos inspectionId, testId, deviceId se pasan correctamente
- [ ] StepsFragment carga steps por testId (loading -> lista o error)
- [ ] Back desde Steps (toolbar o Complete) vuelve a la vista de tests con estado de expansión preservado
- [ ] Al volver (back o Complete), el estado del test se actualiza en la lista (refresh)
- [ ] Tests generados por device type tienen steps predefinidos (template steps)

## Vista Steps (T5.1.2 / T5.1.3 / T5.1.4)
- [ ] Header "Pasos del Test" y toolbar con back
- [ ] Lista de steps con índice (#1, #2...) y nombre
- [ ] BINARY: dropdown Sí/No, guardado al seleccionar
- [ ] DATE_RANGE: dos date pickers (Desde/Hasta), validación from <= to
- [ ] SIMPLE_VALUE: input texto, guardado con debounce
- [ ] NUMERIC_RANGE: input numérico con min/max, validación de rango
- [ ] MULTI_VALUE: 3 campos, todos requeridos para completar
- [ ] N/A por step: checkbox deshabilita inputs y excluye de validación
- [ ] Validaciones en tiempo real: mensajes de error debajo del campo
- [ ] Botón Completar deshabilitado si hay steps obligatorios inválidos o incompletos
- [ ] Botón Completar habilitado cuando todos los steps aplicables son válidos
- [ ] Al cambiar cualquier step se recalcula estado del test
- [ ] Fallback a Room si backend falla o no responde
- [ ] Agregar observación REMARKS: se guarda, no afecta estado
- [ ] Agregar deficiencia DEFICIENCIES: step y test pasan a FAILED, lista se actualiza
- [ ] Step inválido (ej. numérico fuera de rango) persiste como FAILED en backend/Room

## Accesibilidad
- [ ] Content descriptions en íconos expand/collapse
- [ ] Content description en chevron de test

## Regresión
- [ ] Crear nueva location desde InspectionLocationsFragment sigue funcionando
- [ ] Navegación desde InspectionDetail sigue funcionando

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
- [ ] StepsFragment muestra placeholder/cargando
- [ ] Back desde Steps vuelve a la vista de tests con estado de expansión preservado

## Accesibilidad
- [ ] Content descriptions en íconos expand/collapse
- [ ] Content description en chevron de test

## Regresión
- [ ] Crear nueva location desde InspectionLocationsFragment sigue funcionando
- [ ] Navegación desde InspectionDetail sigue funcionando

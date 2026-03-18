-- ============================================================
-- Datos de ejemplo para Inspections API backend
-- Adaptado de database/data-ejemplo.sql para H2 (snake_case)
-- El usuario admin se crea en DataInitializer (admin@inspections.com)
-- ============================================================

-- Buildings (catálogo de edificios para inspecciones building-wide)
INSERT INTO buildings (id, name, details, created_at, updated_at) VALUES
('bld-001', 'Hospital Central', 'Hospital general con sala de emergencias y quirófanos', '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('bld-002', 'Depósito Logístico Norte', 'Almacén y nave de distribución', '2025-01-15 09:00:00', '2025-01-15 09:00:00'),
('bld-003', 'Clínica Sur', 'Clínica con bloque quirúrgico e internación', '2025-01-20 10:00:00', '2025-01-20 10:00:00'),
('bld-004', 'Colegio San Martín', 'Institución educativa primaria y secundaria', '2025-01-25 08:00:00', '2025-01-25 08:00:00'),
('bld-005', 'Torre Oficinas Centro', 'Edificio corporativo 15 pisos', '2025-02-01 09:00:00', '2025-02-01 09:00:00'),
('bld-006', 'Shopping Plaza Norte', 'Centro comercial con 120 locales', '2025-02-05 10:00:00', '2025-02-05 10:00:00'),
('bld-007', 'Laboratorio Farmacéutico', 'Planta de producción y control de calidad', '2025-02-10 08:00:00', '2025-02-10 08:00:00'),
('bld-008', 'Planta Industrial Este', 'Fábrica y almacén de materias primas', '2025-02-15 09:00:00', '2025-02-15 09:00:00'),
('bld-009', 'Hotel Gran Vista', 'Hotel 4 estrellas 200 habitaciones', '2025-02-20 10:00:00', '2025-02-20 10:00:00'),
('bld-010', 'Universidad Tecnológica', 'Campus con laboratorios y talleres', '2025-02-25 08:00:00', '2025-02-25 08:00:00');

-- Inspection templates (catálogo para tipos de inspección)
INSERT INTO inspection_templates (id, code, name, description, enabled, sort_order) VALUES
('tpl-001', 'DAILY_STANDARD', 'Plantilla diaria estándar', 'Inspección diaria de equipos críticos', TRUE, 1),
('tpl-002', 'WEEKLY_STANDARD', 'Plantilla semanal estándar', 'Inspección semanal completa', TRUE, 2),
('tpl-003', 'ANNUAL_FULL', 'Plantilla anual completa', 'Inspección anual exhaustiva', TRUE, 3);

-- Locations (building_id es ref lógica)
INSERT INTO locations (id, building_id, name, details, created_at, updated_at) VALUES
('loc-001', 'bld-001', 'Sala de emergencias', 'Sala principal de emergencias - Planta Baja', '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('loc-002', 'bld-001', 'Sala de bombas', 'Sala de equipos contra incendios - Sótano 1', '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('loc-003', 'bld-001', 'Piso 1 - Corredor Norte', 'Corredor principal con detectores de humo', '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('loc-004', 'bld-001', 'Piso 2 - Oficinas', 'Área de oficinas administrativas', '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('loc-005', 'bld-002', 'Nave principal', 'Nave de almacenamiento - zona A', '2025-01-15 09:00:00', '2025-01-15 09:00:00'),
('loc-006', 'bld-002', 'Sala de máquinas', 'Equipos HVAC y bombas', '2025-01-15 09:00:00', '2025-01-15 09:00:00'),
('loc-007', 'bld-003', 'Quirófanos', 'Bloque quirúrgico - 3 salas', '2025-01-20 10:00:00', '2025-01-20 10:00:00'),
('loc-008', 'bld-003', 'Área de internación', 'Habitaciones 101-120', '2025-01-20 10:00:00', '2025-01-20 10:00:00'),
('loc-009', 'bld-004', 'Aulas bloque A', 'Aulas 1-12 planta baja', '2025-01-25 08:00:00', '2025-01-25 08:00:00'),
('loc-010', 'bld-004', 'Laboratorio informática', 'Sala de computación', '2025-01-25 08:00:00', '2025-01-25 08:00:00'),
('loc-011', 'bld-004', 'Gimnasio', 'Instalaciones deportivas', '2025-01-25 08:00:00', '2025-01-25 08:00:00'),
('loc-012', 'bld-005', 'Piso 1 recepción', 'Hall y recepción', '2025-02-01 09:00:00', '2025-02-01 09:00:00'),
('loc-013', 'bld-005', 'Piso 5 oficinas', 'Área administrativa', '2025-02-01 09:00:00', '2025-02-01 09:00:00'),
('loc-014', 'bld-005', 'Sótano estacionamiento', 'Estacionamiento y sala de bombas', '2025-02-01 09:00:00', '2025-02-01 09:00:00'),
('loc-015', 'bld-006', 'Pasillo principal', 'Corredor central comercial', '2025-02-05 10:00:00', '2025-02-05 10:00:00'),
('loc-016', 'bld-006', 'Sala de máquinas', 'HVAC y equipos', '2025-02-05 10:00:00', '2025-02-05 10:00:00'),
('loc-017', 'bld-007', 'Sala limpia', 'Área de producción estéril', '2025-02-10 08:00:00', '2025-02-10 08:00:00'),
('loc-018', 'bld-007', 'Almacén de reactivos', 'Depósito controlado', '2025-02-10 08:00:00', '2025-02-10 08:00:00'),
('loc-019', 'bld-008', 'Nave de producción', 'Línea de montaje', '2025-02-15 09:00:00', '2025-02-15 09:00:00'),
('loc-020', 'bld-008', 'Sala de compresores', 'Equipos neumáticos', '2025-02-15 09:00:00', '2025-02-15 09:00:00'),
('loc-021', 'bld-009', 'Recepción hotel', 'Lobby y check-in', '2025-02-20 10:00:00', '2025-02-20 10:00:00'),
('loc-022', 'bld-009', 'Piso 3 habitaciones', 'Habitaciones 301-320', '2025-02-20 10:00:00', '2025-02-20 10:00:00'),
('loc-023', 'bld-010', 'Laboratorio electrónica', 'Taller y laboratorio', '2025-02-25 08:00:00', '2025-02-25 08:00:00'),
('loc-024', 'bld-010', 'Biblioteca', 'Sala de lectura y depósito', '2025-02-25 08:00:00', '2025-02-25 08:00:00');

-- Device types (global catalog: extintores, detectores, rociadores, etc.)
INSERT INTO device_types (id, code, name, category, enabled, sort_order) VALUES
('dt-001', 'FACP', 'Panel de alarma', 'FACP_DEVICE', TRUE, 1),
('dt-002', 'JOCKEY_PUMP', 'Bomba jockey', 'JOCKEY_PUMP', TRUE, 2),
('dt-003', 'FIRE_PUMP', 'Bomba contra incendios', 'FIRE_PUMP', TRUE, 3),
('dt-004', 'SMOKE_DETECTOR', 'Detector de humo', 'FA_FIELD_DEVICE', TRUE, 4),
('dt-005', 'EXTINGUISHER', 'Extintor', 'SPRINKLER_DEVICE', TRUE, 5),
('dt-006', 'SPRINKLER_HEAD', 'Rociador', 'SPRINKLER_DEVICE', TRUE, 6),
('dt-007', 'DEFIBRILLATOR', 'Desfibrilador', 'FA_FIELD_DEVICE', TRUE, 7),
('dt-008', 'VITAL_SIGNS_MONITOR', 'Monitor de signos vitales', 'FA_FIELD_DEVICE', TRUE, 8),
('dt-009', 'HEAT_DETECTOR', 'Detector de calor', 'FA_FIELD_DEVICE', TRUE, 9),
('dt-010', 'PUMP_CONTROLLER', 'Controlador de bomba', 'PUMP_CONTROLLER', TRUE, 10);

-- Test templates (catalog for inherited tests)
INSERT INTO test_templates (id, code, name, description, enabled, sort_order) VALUES
('tt-001', 'FACP_VERIFY', 'Verificación FACP', 'Revisión del panel de alarma', TRUE, 1),
('tt-002', 'JOCKEY_PUMP_TEST', 'Prueba Jockey Pump', 'Verificación de presión', TRUE, 2),
('tt-003', 'SMOKE_DETECTOR_TEST', 'Prueba detector humo', 'Verificación detector de humo', TRUE, 3),
('tt-004', 'DEFIBRILLATOR_TEST', 'Prueba desfibrilador', 'Verificación de carga y electrodos', TRUE, 4),
('tt-005', 'MONITOR_CALIBRATION', 'Calibración monitor', 'Verificar lecturas de SpO2 y ECG', TRUE, 5),
('tt-006', 'EXTINGUISHER_INSPECT', 'Inspección extintor', 'Verificar sello, manómetro y fecha vencimiento', TRUE, 6),
('tt-007', 'FACP_SIREN_TEST', 'Test de sirena FACP', 'Activar sirena y verificar audibilidad', TRUE, 7),
('tt-008', 'FIRE_PUMP_TEST', 'Prueba Fire Pump', 'Verificación de arranque y presión nominal', TRUE, 8),
('tt-009', 'JOCKEY_VALVES_VERIFY', 'Verificación válvulas Jockey', 'Revisión de válvulas de aislamiento', TRUE, 9),
('tt-010', 'HEAT_DETECTOR_TEST', 'Prueba detector calor', 'Test funcional con fuente de calor', TRUE, 10),
('tt-011', 'MONITOR_GAS_CALIBRATION', 'Calibración monitor gases', 'Verificar sensor gases anestésicos', TRUE, 11),
('tt-012', 'SPRINKLER_HEAD_TEST', 'Prueba rociador', 'Verificar alineación y obstrucción', TRUE, 12),
('tt-013', 'PUMP_CONTROLLER_VERIFY', 'Verificación controlador bomba', 'Revisión de indicadores y alarmas', TRUE, 13);

-- Device type -> test template mappings (inherited tests per device type)
INSERT INTO device_type_test_templates (device_type_id, test_template_id, sort_order) VALUES
('dt-001', 'tt-001', 1),
('dt-001', 'tt-007', 2),
('dt-002', 'tt-002', 1),
('dt-002', 'tt-009', 2),
('dt-003', 'tt-008', 1),
('dt-004', 'tt-003', 1),
('dt-005', 'tt-006', 1),
('dt-006', 'tt-012', 1),
('dt-007', 'tt-004', 1),
('dt-008', 'tt-005', 1),
('dt-008', 'tt-011', 2),
('dt-009', 'tt-010', 1),
('dt-010', 'tt-013', 1);

-- Test template steps (step definitions per template; cloned when creating tests)
INSERT INTO test_template_steps (id, test_template_id, name, test_step_type, description, min_value, max_value, sort_order) VALUES
('tts-001', 'tt-001', 'Lectura de display', 'SIMPLE_VALUE', 'Verificar lectura en display principal', NULL, NULL, 1),
('tts-002', 'tt-001', 'Alarma en verde', 'BINARY', 'Indicador de alarma en estado normal', NULL, NULL, 2),
('tts-003', 'tt-002', 'Presión (psi)', 'RANGE', 'Medir presión en salida', 50.0, 120.0, 1),
('tts-004', 'tt-003', 'Prueba de humo', 'BINARY', 'Simular humo y verificar alarma', NULL, NULL, 1),
('tts-005', 'tt-004', 'Nivel de carga batería', 'RANGE', 'Verificar carga suficiente', 80.0, 100.0, 1),
('tts-006', 'tt-004', 'Estado electrodos', 'BINARY', 'Electrodos no vencidos', NULL, NULL, 2),
('tts-007', 'tt-005', 'Lectura SpO2', 'RANGE', 'Verificar rango SpO2', 95.0, 100.0, 1),
('tts-008', 'tt-005', 'Lectura ECG', 'SIMPLE_VALUE', 'Verificar trazado ECG', NULL, NULL, 2),
('tts-009', 'tt-006', 'Sello intacto', 'BINARY', 'Verificar que el sello está intacto', NULL, NULL, 1),
('tts-010', 'tt-006', 'Manómetro en zona verde', 'BINARY', 'Presión en rango operativo', NULL, NULL, 2),
('tts-011', 'tt-007', 'Sirena audible', 'BINARY', 'Activar sirena y verificar audibilidad', NULL, NULL, 1),
('tts-012', 'tt-008', 'Presión nominal (psi)', 'RANGE', 'Verificar presión de descarga', 100.0, 150.0, 1),
('tts-013', 'tt-008', 'Arranque correcto', 'BINARY', 'Bomba arranca sin fallos', NULL, NULL, 2),
('tts-014', 'tt-009', 'Válvulas operativas', 'BINARY', 'Válvulas de aislamiento OK', NULL, NULL, 1),
('tts-015', 'tt-010', 'Respuesta a calor', 'BINARY', 'Detector activa al alcanzar umbral', NULL, NULL, 1),
('tts-016', 'tt-011', 'Lectura sensor gases', 'RANGE', 'Calibración gases anestésicos', 0.0, 5.0, 1),
('tts-017', 'tt-012', 'Alineación rociador', 'BINARY', 'Rociador alineado correctamente', NULL, NULL, 1),
('tts-018', 'tt-013', 'Indicadores OK', 'BINARY', 'Revisión de indicadores y alarmas', NULL, NULL, 1);

-- Zones (FK: location_id -> locations)
INSERT INTO zones (id, location_id, name, details) VALUES
('zone-001', 'loc-001', 'Equipo médico', 'Desfibriladores y equipos de primeros auxilios'),
('zone-002', 'loc-001', 'Sistemas de seguridad', 'Panel de alarma y extintores'),
('zone-003', 'loc-002', 'Bombas contra incendios', 'Jockey pump y fire pump'),
('zone-004', 'loc-003', 'Detectores', 'Detectores de humo y calor en corredor'),
('zone-005', 'loc-004', 'Extintores', 'Extintores tipo ABC en oficinas'),
('zone-006', 'loc-005', 'Rociadores', 'Sistema de rociadores automáticos'),
('zone-007', 'loc-006', 'Bombas', 'Bombas de incendio y jockey'),
('zone-008', 'loc-007', 'Quirófano 1', 'Sala principal con equipos médicos'),
('zone-009', 'loc-008', 'Pasillo central', 'Detectores y extintores'),
('zone-010', 'loc-009', 'Aula 1-4', 'Zona norte aulas'),
('zone-011', 'loc-009', 'Aula 5-8', 'Zona sur aulas'),
('zone-012', 'loc-010', 'Sala servidores', 'Rack y equipos'),
('zone-013', 'loc-011', 'Cancha', 'Área deportiva'),
('zone-014', 'loc-012', 'Hall principal', 'Recepción y detectores'),
('zone-015', 'loc-013', 'Oficinas abiertas', 'Cubículos'),
('zone-016', 'loc-014', 'Bombas', 'Sala de bombas incendio'),
('zone-017', 'loc-015', 'Pasillo nivel 1', 'Corredor comercial'),
('zone-018', 'loc-016', 'HVAC', 'Equipos climatización'),
('zone-019', 'loc-017', 'Línea 1', 'Producción estéril'),
('zone-020', 'loc-018', 'Estanterías A', 'Almacenamiento'),
('zone-021', 'loc-019', 'Línea montaje', 'Estación de trabajo'),
('zone-022', 'loc-020', 'Compresores', 'Sala de equipos'),
('zone-023', 'loc-021', 'Lobby', 'Recepción y extintores'),
('zone-024', 'loc-022', 'Pasillo 3', 'Corredor habitaciones'),
('zone-025', 'loc-023', 'Taller', 'Bancos de trabajo'),
('zone-026', 'loc-024', 'Sala lectura', 'Área de estudio');

-- Devices (FK: zone_id -> zones, location_id -> locations)
INSERT INTO devices (id, zone_id, location_id, building_id, device_type_id, device_category, name, description, device_serial_number, installation_date, expiration_date, enabled, attribute_ids, created_at, updated_at) VALUES
('dev-001', 'zone-002', 'loc-001', 'bld-001', 'dt-001', 'FACP_DEVICE', 'Panel de alarma principal', 'Panel de control FACP - Sala emergencias', 1001, '2024-01-15', '2026-01-15', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('dev-002', 'zone-003', 'loc-002', 'bld-001', 'dt-002', 'JOCKEY_PUMP', 'Jockey Pump A', 'Bomba de mantenimiento de presión', 2001, '2024-03-01', '2025-09-01', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('dev-003', 'zone-003', 'loc-002', 'bld-001', 'dt-003', 'FIRE_PUMP', 'Fire Pump principal', 'Bomba principal contra incendios', 3001, '2024-02-10', '2026-02-10', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('dev-004', 'zone-004', 'loc-003', 'bld-001', 'dt-004', 'FA_FIELD_DEVICE', 'Detector de humo DH-01', 'Detector fotoeléctrico corredor norte', 4001, '2024-05-20', '2027-05-20', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('dev-005', 'zone-005', 'loc-004', 'bld-001', 'dt-005', 'SPRINKLER_DEVICE', 'Extintor ABC 6kg', 'Extintor polvo químico oficinas', 5001, '2024-06-01', '2025-06-01', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('dev-006', 'zone-006', 'loc-005', 'bld-002', 'dt-006', 'SPRINKLER_DEVICE', 'Rociador cabeza 1', 'Rociador tipo pendiente nave A', 6001, '2024-04-15', '2029-04-15', TRUE, NULL, '2025-01-15 09:00:00', '2025-01-15 09:00:00'),
('dev-007', 'zone-007', 'loc-006', 'bld-002', 'dt-002', 'JOCKEY_PUMP', 'Jockey Pump B', 'Bomba mantenimiento depósito', 2002, '2024-07-01', '2025-07-01', TRUE, NULL, '2025-01-15 09:00:00', '2025-01-15 09:00:00'),
('dev-008', 'zone-008', 'loc-007', 'bld-003', 'dt-001', 'FACP_DEVICE', 'Panel quirófanos', 'Panel de alarma bloque quirúrgico', 1002, '2024-08-10', '2026-08-10', TRUE, NULL, '2025-01-20 10:00:00', '2025-01-20 10:00:00'),
('dev-009', 'zone-009', 'loc-008', 'bld-003', 'dt-004', 'FA_FIELD_DEVICE', 'Detector pasillo 101', 'Detector área internación', 4002, '2024-09-01', '2027-09-01', TRUE, NULL, '2025-01-20 10:00:00', '2025-01-20 10:00:00'),
('dev-010', 'zone-001', 'loc-001', 'bld-001', 'dt-007', 'FA_FIELD_DEVICE', 'Desfibrilador DEA-01', 'Desfibrilador automático sala emergencias', 7001, '2024-02-01', '2027-02-01', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('dev-011', 'zone-001', 'loc-001', 'bld-001', 'dt-008', 'FA_FIELD_DEVICE', 'Monitor de signos vitales MV-03', 'Monitor multiparámetro cabecera', 7002, '2024-04-10', '2028-04-10', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('dev-012', 'zone-002', 'loc-001', 'bld-001', 'dt-009', 'SPRINKLER_DEVICE', 'Extintor CO2 5kg', 'Extintor de CO2 junto a panel FACP', 5002, '2024-03-15', '2025-09-15', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('dev-013', 'zone-004', 'loc-003', 'bld-001', 'dt-004', 'FA_FIELD_DEVICE', 'Detector de calor DC-01', 'Detector térmico corredor norte', 4003, '2024-06-01', '2027-06-01', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('dev-014', 'zone-005', 'loc-004', 'bld-001', 'dt-001', 'FACP_DEVICE', 'Panel secundario oficinas', 'Panel de alarma zona oficinas', 1003, '2024-05-01', '2026-05-01', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('dev-015', 'zone-006', 'loc-005', 'bld-002', 'dt-006', 'SPRINKLER_DEVICE', 'Rociador cabeza 2', 'Rociador tipo montante nave A', 6002, '2024-04-15', '2029-04-15', TRUE, NULL, '2025-01-15 09:00:00', '2025-01-15 09:00:00'),
('dev-016', 'zone-008', 'loc-007', 'bld-003', 'dt-008', 'FA_FIELD_DEVICE', 'Monitor anestesia Q1', 'Monitor gases anestésicos quirófano 1', 7003, '2024-09-01', '2028-09-01', TRUE, NULL, '2025-01-20 10:00:00', '2025-01-20 10:00:00'),
('dev-017', 'zone-009', 'loc-008', 'bld-003', 'dt-005', 'SPRINKLER_DEVICE', 'Extintor pasillo internación', 'Extintor ABC 4kg pasillo central', 5003, '2024-10-01', '2025-10-01', TRUE, NULL, '2025-01-20 10:00:00', '2025-01-20 10:00:00'),
('dev-018', 'zone-010', 'loc-009', 'bld-004', 'dt-005', 'SPRINKLER_DEVICE', 'Extintor aula 1', 'Extintor ABC 6kg', 5004, '2024-06-01', '2025-06-01', TRUE, NULL, '2025-01-25 08:00:00', '2025-01-25 08:00:00'),
('dev-019', 'zone-010', 'loc-009', 'bld-004', 'dt-004', 'FA_FIELD_DEVICE', 'Detector aula 2', 'Detector fotoeléctrico', 4004, '2024-05-20', '2027-05-20', TRUE, NULL, '2025-01-25 08:00:00', '2025-01-25 08:00:00'),
('dev-020', 'zone-011', 'loc-009', 'bld-004', 'dt-005', 'SPRINKLER_DEVICE', 'Extintor aula 6', 'Extintor ABC 6kg', 5005, '2024-06-01', '2025-06-01', TRUE, NULL, '2025-01-25 08:00:00', '2025-01-25 08:00:00'),
('dev-021', 'zone-012', 'loc-010', 'bld-004', 'dt-001', 'FACP_DEVICE', 'Panel informática', 'Panel de alarma sala', 1004, '2024-08-01', '2026-08-01', TRUE, NULL, '2025-01-25 08:00:00', '2025-01-25 08:00:00'),
('dev-022', 'zone-014', 'loc-012', 'bld-005', 'dt-004', 'FA_FIELD_DEVICE', 'Detector recepción', 'Detector hall principal', 4005, '2024-05-20', '2027-05-20', TRUE, NULL, '2025-02-01 09:00:00', '2025-02-01 09:00:00'),
('dev-023', 'zone-014', 'loc-012', 'bld-005', 'dt-005', 'SPRINKLER_DEVICE', 'Extintor recepción', 'Extintor ABC 6kg', 5006, '2024-06-01', '2025-06-01', TRUE, NULL, '2025-02-01 09:00:00', '2025-02-01 09:00:00'),
('dev-024', 'zone-015', 'loc-013', 'bld-005', 'dt-004', 'FA_FIELD_DEVICE', 'Detector oficinas', 'Detector zona abierta', 4006, '2024-05-20', '2027-05-20', TRUE, NULL, '2025-02-01 09:00:00', '2025-02-01 09:00:00'),
('dev-025', 'zone-016', 'loc-014', 'bld-005', 'dt-002', 'JOCKEY_PUMP', 'Jockey Pump torre', 'Bomba mantenimiento presión', 2003, '2024-07-01', '2025-07-01', TRUE, NULL, '2025-02-01 09:00:00', '2025-02-01 09:00:00'),
('dev-026', 'zone-017', 'loc-015', 'bld-006', 'dt-004', 'FA_FIELD_DEVICE', 'Detector pasillo shopping', 'Detector corredor', 4007, '2024-05-20', '2027-05-20', TRUE, NULL, '2025-02-05 10:00:00', '2025-02-05 10:00:00'),
('dev-027', 'zone-017', 'loc-015', 'bld-006', 'dt-005', 'SPRINKLER_DEVICE', 'Extintor pasillo', 'Extintor ABC 6kg', 5007, '2024-06-01', '2025-06-01', TRUE, NULL, '2025-02-05 10:00:00', '2025-02-05 10:00:00'),
('dev-028', 'zone-018', 'loc-016', 'bld-006', 'dt-002', 'JOCKEY_PUMP', 'Jockey Pump shopping', 'Bomba HVAC', 2004, '2024-07-01', '2025-07-01', TRUE, NULL, '2025-02-05 10:00:00', '2025-02-05 10:00:00'),
('dev-029', 'zone-019', 'loc-017', 'bld-007', 'dt-001', 'FACP_DEVICE', 'Panel sala limpia', 'Panel área estéril', 1005, '2024-08-01', '2026-08-01', TRUE, NULL, '2025-02-10 08:00:00', '2025-02-10 08:00:00'),
('dev-030', 'zone-019', 'loc-017', 'bld-007', 'dt-005', 'SPRINKLER_DEVICE', 'Extintor sala limpia', 'Extintor CO2', 5008, '2024-06-01', '2025-06-01', TRUE, NULL, '2025-02-10 08:00:00', '2025-02-10 08:00:00'),
('dev-031', 'zone-020', 'loc-018', 'bld-007', 'dt-004', 'FA_FIELD_DEVICE', 'Detector almacén', 'Detector reactivos', 4008, '2024-05-20', '2027-05-20', TRUE, NULL, '2025-02-10 08:00:00', '2025-02-10 08:00:00'),
('dev-032', 'zone-021', 'loc-019', 'bld-008', 'dt-005', 'SPRINKLER_DEVICE', 'Extintor línea 1', 'Extintor ABC 9kg', 5009, '2024-06-01', '2025-06-01', TRUE, NULL, '2025-02-15 09:00:00', '2025-02-15 09:00:00'),
('dev-033', 'zone-021', 'loc-019', 'bld-008', 'dt-006', 'SPRINKLER_DEVICE', 'Rociador cabeza línea', 'Rociador pendiente', 6003, '2024-04-15', '2029-04-15', TRUE, NULL, '2025-02-15 09:00:00', '2025-02-15 09:00:00'),
('dev-034', 'zone-022', 'loc-020', 'bld-008', 'dt-010', 'PUMP_CONTROLLER', 'Controlador compresores', 'Controlador sala neumática', 10001, '2024-05-01', '2026-05-01', TRUE, NULL, '2025-02-15 09:00:00', '2025-02-15 09:00:00'),
('dev-035', 'zone-023', 'loc-021', 'bld-009', 'dt-005', 'SPRINKLER_DEVICE', 'Extintor lobby', 'Extintor ABC 6kg', 5010, '2024-06-01', '2025-06-01', TRUE, NULL, '2025-02-20 10:00:00', '2025-02-20 10:00:00'),
('dev-036', 'zone-023', 'loc-021', 'bld-009', 'dt-004', 'FA_FIELD_DEVICE', 'Detector lobby', 'Detector recepción', 4009, '2024-05-20', '2027-05-20', TRUE, NULL, '2025-02-20 10:00:00', '2025-02-20 10:00:00'),
('dev-037', 'zone-024', 'loc-022', 'bld-009', 'dt-004', 'FA_FIELD_DEVICE', 'Detector pasillo 3', 'Detector habitaciones', 4010, '2024-05-20', '2027-05-20', TRUE, NULL, '2025-02-20 10:00:00', '2025-02-20 10:00:00'),
('dev-038', 'zone-024', 'loc-022', 'bld-009', 'dt-005', 'SPRINKLER_DEVICE', 'Extintor pasillo 3', 'Extintor ABC 4kg', 5011, '2024-06-01', '2025-06-01', TRUE, NULL, '2025-02-20 10:00:00', '2025-02-20 10:00:00'),
('dev-039', 'zone-025', 'loc-023', 'bld-010', 'dt-001', 'FACP_DEVICE', 'Panel laboratorio', 'Panel taller electrónica', 1006, '2024-08-01', '2026-08-01', TRUE, NULL, '2025-02-25 08:00:00', '2025-02-25 08:00:00'),
('dev-040', 'zone-025', 'loc-023', 'bld-010', 'dt-005', 'SPRINKLER_DEVICE', 'Extintor taller', 'Extintor ABC 6kg', 5012, '2024-06-01', '2025-06-01', TRUE, NULL, '2025-02-25 08:00:00', '2025-02-25 08:00:00'),
('dev-041', 'zone-026', 'loc-024', 'bld-010', 'dt-004', 'FA_FIELD_DEVICE', 'Detector biblioteca', 'Detector sala lectura', 4011, '2024-05-20', '2027-05-20', TRUE, NULL, '2025-02-25 08:00:00', '2025-02-25 08:00:00');

-- Inspections (building-wide: location_id NULL; building_id es ref lógica)
-- Solo 3 inspecciones demo: PENDING (bld-001), IN_PROGRESS (bld-002), DONE_COMPLETED (bld-003)
INSERT INTO inspections (id, building_id, location_id, type, status, scheduled_date, approval_date, result, notes, signer, signed, sign_date, started_at, inspection_report_id, inspection_template_id, cover_page_id, created_at, updated_at) VALUES
('insp-001', 'bld-001', NULL, 'Weekly', 'PENDING', '2025-03-20 08:00:00', NULL, NULL, 'Inspección semanal - Hospital Central (programada)', NULL, FALSE, NULL, NULL, NULL, 'tpl-001', NULL, '2025-03-10 09:00:00', '2025-03-10 09:00:00'),
('insp-002', 'bld-002', NULL, 'Monthly', 'IN_PROGRESS', '2025-03-18 10:00:00', NULL, NULL, 'Inspección mensual depósito - en curso', NULL, FALSE, NULL, '2025-03-18 09:00:00', NULL, 'tpl-002', NULL, '2025-03-15 09:00:00', '2025-03-18 09:00:00'),
('insp-003', 'bld-003', NULL, 'Weekly', 'DONE_COMPLETED', '2025-03-15 09:00:00', '2025-03-15 16:00:00', 'SUCCESS', 'Inspección semanal Clínica Sur - completada OK', 'María García', TRUE, '2025-03-15 16:00:00', '2025-03-15 09:00:00', 'rpt-001', 'tpl-002', NULL, '2025-03-12 09:00:00', '2025-03-15 16:00:00');

-- Inspection assignments (FK: inspection_id -> inspections)
INSERT INTO inspection_assignments (id, inspection_id, user_email, role, created_at) VALUES
('asgn-001', 'insp-001', 'inspector@example.com', 'INSPECTOR', '2025-03-10 09:00:00'),
('asgn-002', 'insp-001', 'operator@example.com', 'OPERATOR', '2025-03-10 09:00:00'),
('asgn-003', 'insp-002', 'inspector@example.com', 'INSPECTOR', '2025-03-15 09:00:00'),
('asgn-004', 'insp-002', 'operator@example.com', 'OPERATOR', '2025-03-15 09:00:00'),
('asgn-005', 'insp-003', 'inspector@example.com', 'INSPECTOR', '2025-03-12 09:00:00');

-- Photos (para observations)
INSERT INTO photos (id, media_url, name, description, file_details_json, local_path, timestamp, inspector_id, step_id, device_id, created_at) VALUES
('photo-001', 'https://example.com/media/photo1.jpg', 'Evidencia_rociador.jpg', 'Foto rociador alineado', '{"size": 245000}', NULL, '2025-03-18 09:15:00', 'admin-001', 'step-008', 'dev-006', '2025-03-18 09:15:00'),
('photo-002', 'https://example.com/media/photo2.jpg', 'Evidencia_Jockey.jpg', 'Foto Jockey Pump presión baja', '{"size": 312000}', NULL, '2025-03-18 09:30:00', 'admin-001', 'step-009', 'dev-007', '2025-03-18 09:30:00');

-- Tests (FK: device_id -> devices, inspection_id -> inspections)
-- insp-001 PENDING (bld-001): mayoría PENDING, 1 COMPLETED para variedad
-- insp-002 IN_PROGRESS (bld-002): mezcla PENDING, COMPLETED, FAILED
-- insp-003 DONE_COMPLETED (bld-003): todos COMPLETED
INSERT INTO tests (id, device_id, inspection_id, test_template_id, test_step_ids, name, description, status, applicable, created_at, updated_at) VALUES
('test-001', 'dev-001', 'insp-001', 'tt-001', '["step-001","step-002"]', 'Verificación FACP', 'Revisión del panel de alarma', 'PENDING', TRUE, '2025-03-10 09:00:00', '2025-03-10 09:00:00'),
('test-002', 'dev-002', 'insp-001', 'tt-002', '["step-003"]', 'Prueba Jockey Pump', 'Verificación de presión', 'PENDING', TRUE, '2025-03-10 09:00:00', '2025-03-10 09:00:00'),
('test-003', 'dev-004', 'insp-001', 'tt-003', '["step-004"]', 'Prueba detector humo', 'Verificación detector DH-01', 'PENDING', TRUE, '2025-03-10 09:00:00', '2025-03-10 09:00:00'),
('test-004', 'dev-005', 'insp-001', 'tt-006', '["step-005"]', 'Inspección extintor ABC', 'Verificar sello y manómetro', 'COMPLETED', TRUE, '2025-03-10 09:00:00', '2025-03-10 10:00:00'),
('test-005', 'dev-010', 'insp-001', 'tt-004', '["step-006","step-007"]', 'Prueba desfibrilador', 'Verificación de carga y electrodos', 'PENDING', TRUE, '2025-03-10 09:00:00', '2025-03-10 09:00:00'),
('test-006', 'dev-006', 'insp-002', 'tt-012', '["step-008"]', 'Prueba rociador cabeza 1', 'Verificar alineación y obstrucción', 'COMPLETED', TRUE, '2025-03-18 09:00:00', '2025-03-18 09:45:00'),
('test-007', 'dev-007', 'insp-002', 'tt-002', '["step-009"]', 'Prueba Jockey Pump B', 'Verificación de presión depósito', 'FAILED', TRUE, '2025-03-18 09:00:00', '2025-03-18 10:00:00'),
('test-008', 'dev-015', 'insp-002', 'tt-012', '["step-010"]', 'Prueba rociador cabeza 2', 'Verificar alineación nave A', 'PENDING', TRUE, '2025-03-18 09:00:00', '2025-03-18 09:00:00'),
('test-009', 'dev-008', 'insp-003', 'tt-001', '["step-011","step-012"]', 'Verificación panel quirófanos', 'Revisión del panel de alarma', 'COMPLETED', TRUE, '2025-03-15 09:00:00', '2025-03-15 16:00:00'),
('test-010', 'dev-009', 'insp-003', 'tt-003', '["step-013"]', 'Prueba detector pasillo 101', 'Verificación detector humo', 'COMPLETED', TRUE, '2025-03-15 09:00:00', '2025-03-15 14:00:00'),
('test-011', 'dev-016', 'insp-003', 'tt-011', '["step-014"]', 'Calibración monitor anestesia', 'Verificar sensor gases anestésicos', 'COMPLETED', TRUE, '2025-03-15 09:00:00', '2025-03-15 15:00:00'),
('test-012', 'dev-017', 'insp-003', 'tt-006', '["step-015"]', 'Inspección extintor pasillo', 'Verificar sello y accesibilidad', 'COMPLETED', TRUE, '2025-03-15 09:00:00', '2025-03-15 15:30:00');

-- Steps (FK: test_id -> tests)
INSERT INTO steps (id, test_id, name, test_step_type, applicable, status, description, value_json, min_value, max_value, created_at, updated_at) VALUES
('step-001', 'test-001', 'Lectura de display', 'SIMPLE_VALUE', TRUE, 'PENDING', 'Verificar lectura en display principal', NULL, NULL, NULL, '2025-03-10 09:00:00', '2025-03-10 09:00:00'),
('step-002', 'test-001', 'Alarma en verde', 'BINARY', TRUE, 'PENDING', 'Indicador de alarma en estado normal', NULL, NULL, NULL, '2025-03-10 09:00:00', '2025-03-10 09:00:00'),
('step-003', 'test-002', 'Presión (psi)', 'RANGE', TRUE, 'PENDING', 'Medir presión en salida', NULL, 50.0, 120.0, '2025-03-10 09:00:00', '2025-03-10 09:00:00'),
('step-004', 'test-003', 'Prueba de humo', 'BINARY', TRUE, 'PENDING', 'Simular humo y verificar alarma', NULL, NULL, NULL, '2025-03-10 09:00:00', '2025-03-10 09:00:00'),
('step-005', 'test-004', 'Sello intacto', 'BINARY', TRUE, 'COMPLETED', 'Verificar que el sello está intacto', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-10 09:00:00', '2025-03-10 10:00:00'),
('step-006', 'test-005', 'Nivel de carga batería', 'RANGE', TRUE, 'PENDING', 'Verificar carga suficiente', NULL, 80.0, 100.0, '2025-03-10 09:00:00', '2025-03-10 09:00:00'),
('step-007', 'test-005', 'Estado electrodos', 'BINARY', TRUE, 'PENDING', 'Electrodos no vencidos', NULL, NULL, NULL, '2025-03-10 09:00:00', '2025-03-10 09:00:00'),
('step-008', 'test-006', 'Alineación rociador', 'BINARY', TRUE, 'COMPLETED', 'Rociador alineado correctamente', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-18 09:00:00', '2025-03-18 09:45:00'),
('step-009', 'test-007', 'Presión (psi)', 'RANGE', TRUE, 'FAILED', 'Presión fuera de rango - falla', '{"value": 35.0, "valueType": "NUMERIC_UNIT_VALUE"}', 50.0, 120.0, '2025-03-18 09:00:00', '2025-03-18 10:00:00'),
('step-010', 'test-008', 'Alineación rociador', 'BINARY', TRUE, 'PENDING', 'Verificar alineación', NULL, NULL, NULL, '2025-03-18 09:00:00', '2025-03-18 09:00:00'),
('step-011', 'test-009', 'Display operativo', 'BINARY', TRUE, 'COMPLETED', 'Display del panel OK', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-15 09:00:00', '2025-03-15 16:00:00'),
('step-012', 'test-009', 'Sin alarmas activas', 'BINARY', TRUE, 'COMPLETED', 'No hay alarmas pendientes', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-15 09:00:00', '2025-03-15 16:00:00'),
('step-013', 'test-010', 'Prueba de humo', 'BINARY', TRUE, 'COMPLETED', 'Detector respondió correctamente', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-15 09:00:00', '2025-03-15 14:00:00'),
('step-014', 'test-011', 'Lectura sensor gases', 'RANGE', TRUE, 'COMPLETED', 'Calibración OK', '{"value": 2.1, "valueType": "NUMERIC_UNIT_VALUE"}', 0.0, 5.0, '2025-03-15 09:00:00', '2025-03-15 15:00:00'),
('step-015', 'test-012', 'Sello y accesibilidad', 'BINARY', TRUE, 'COMPLETED', 'Extintor accesible y sello intacto', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-15 09:00:00', '2025-03-15 15:30:00');

-- Observations (FK: test_step_id -> steps, inspection_id -> inspections)
INSERT INTO observations (id, test_step_id, inspection_id, name, type, description, deficiency_type_id, media_id, created_at, updated_at) VALUES
('obs-001', 'step-008', 'insp-002', 'Evidencia visual', 'REMARKS', 'Rociador alineado correctamente', NULL, 'photo-001', '2025-03-18 09:15:00', '2025-03-18 09:15:00'),
('obs-002', 'step-009', 'insp-002', 'Deficiencia detectada', 'DEFICIENCY', 'Presión baja - requiere revisión de bomba', 'def-001', 'photo-002', '2025-03-18 10:00:00', '2025-03-18 10:00:00');

-- Audit logs (user_id referencia admin creado en DataInitializer)
INSERT INTO audit_logs (id, user_id, entity_type, entity_id, action, metadata_json, created_at) VALUES
('log-001', 'admin-001', 'Inspection', 'insp-001', 'CREATE', '{"ip": "192.168.1.10"}', '2025-03-10 09:00:00'),
('log-002', 'admin-001', 'Inspection', 'insp-002', 'START', '{"startedAt": "2025-03-18T09:00:00Z"}', '2025-03-18 09:00:00'),
('log-003', 'admin-001', 'Inspection', 'insp-003', 'SIGN', '{"signer": "María García"}', '2025-03-15 16:00:00');

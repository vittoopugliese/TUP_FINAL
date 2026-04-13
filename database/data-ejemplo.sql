-- ============================================================
-- Datos de ejemplo para Inspections Android app
-- Tablas: users, locations, zones, devices, inspections,
--         inspection_assignments, photos, tests, steps,
--         observations, audit_logs
-- Orden respeta dependencias de foreign keys
--
-- Backend: Este archivo está adaptado en backend/src/main/resources/data.sql (H2/snake_case)
-- Usuarios de ejemplo: mismos 4 que crea DataInitializer.java (admin, inspector, 2 operadores).
-- ============================================================

-- Users (no FKs) — alineados con backend DataInitializer (ids locales usr-* solo para este script)
INSERT INTO users (id, email, firstName, lastName, avatarImage, phoneNumber, role, lastLoginAt, createdAt) VALUES
('usr-001', 'inspector@example.com', 'María', 'García', 'https://example.com/avatars/1.jpg', '+54 11 1234-5678', 'INSPECTOR', '2025-03-14T10:00:00Z', '2025-01-15T09:00:00Z'),
('usr-002', 'operador@inspections.com', 'Operador', 'Sistema', NULL, '+54 11 8765-4321', 'OPERATOR', '2025-03-13T14:30:00Z', '2025-01-20T11:00:00Z'),
('usr-003', 'admin@inspections.com', 'Admin', 'Inspector', NULL, '+54 11 5555-0000', 'ADMIN', '2025-03-14T08:00:00Z', '2025-01-01T00:00:00Z'),
('usr-004', 'operador1@inspections.com', 'Operador', 'Uno', NULL, '+54 11 2222-1111', 'OPERATOR', '2025-03-11T09:00:00Z', '2025-02-05T14:00:00Z');

-- Locations (buildingId es ref lógica; múltiples edificios y ubicaciones)
INSERT INTO locations (id, buildingId, name, details, createdAt, updatedAt) VALUES
-- Edificio bld-001 (Torre Central)
('loc-001', 'bld-001', 'Sala de emergencias', 'Sala principal de emergencias - Planta Baja', '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
('loc-002', 'bld-001', 'Sala de bombas', 'Sala de equipos contra incendios - Sótano 1', '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
('loc-003', 'bld-001', 'Piso 1 - Corredor Norte', 'Corredor principal con detectores de humo', '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
('loc-004', 'bld-001', 'Piso 2 - Oficinas', 'Área de oficinas administrativas', '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
-- Edificio bld-002 (Depósito Industrial)
('loc-005', 'bld-002', 'Nave principal', 'Nave de almacenamiento - zona A', '2025-01-15T09:00:00Z', '2025-01-15T09:00:00Z'),
('loc-006', 'bld-002', 'Sala de máquinas', 'Equipos HVAC y bombas', '2025-01-15T09:00:00Z', '2025-01-15T09:00:00Z'),
-- Edificio bld-003 (Clínica)
('loc-007', 'bld-003', 'Quirófanos', 'Bloque quirúrgico - 3 salas', '2025-01-20T10:00:00Z', '2025-01-20T10:00:00Z'),
('loc-008', 'bld-003', 'Área de internación', 'Habitaciones 101-120', '2025-01-20T10:00:00Z', '2025-01-20T10:00:00Z');

-- Zones (FK: locationId -> locations)
INSERT INTO zones (id, locationId, name, details) VALUES
('zone-001', 'loc-001', 'Equipo médico', 'Desfibriladores y equipos de primeros auxilios'),
('zone-002', 'loc-001', 'Sistemas de seguridad', 'Panel de alarma y extintores'),
('zone-003', 'loc-002', 'Bombas contra incendios', 'Jockey pump y fire pump'),
('zone-004', 'loc-003', 'Detectores', 'Detectores de humo y calor en corredor'),
('zone-005', 'loc-004', 'Extintores', 'Extintores tipo ABC en oficinas'),
('zone-006', 'loc-005', 'Rociadores', 'Sistema de rociadores automáticos'),
('zone-007', 'loc-006', 'Bombas', 'Bombas de incendio y jockey'),
('zone-008', 'loc-007', 'Quirófano 1', 'Sala principal con equipos médicos'),
('zone-009', 'loc-008', 'Pasillo central', 'Detectores y extintores');

-- Devices (FK: zoneId -> zones, locationId -> locations)
INSERT INTO devices (id, zoneId, locationId, buildingId, deviceTypeId, deviceCategory, name, description, deviceSerialNumber, installationDate, expirationDate, enabled, attributeIds, createdAt, updatedAt) VALUES
('dev-001', 'zone-002', 'loc-001', 'bld-001', 'dt-001', 'FACP_DEVICE', 'Panel de alarma principal', 'Panel de control FACP - Sala emergencias', 1001, '2024-01-15', '2026-01-15', 1, NULL, '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
('dev-002', 'zone-003', 'loc-002', 'bld-001', 'dt-002', 'JOCKEY_PUMP', 'Jockey Pump A', 'Bomba de mantenimiento de presión', 2001, '2024-03-01', '2025-09-01', 1, NULL, '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
('dev-003', 'zone-003', 'loc-002', 'bld-001', 'dt-003', 'FIRE_PUMP', 'Fire Pump principal', 'Bomba principal contra incendios', 3001, '2024-02-10', '2026-02-10', 1, NULL, '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
('dev-004', 'zone-004', 'loc-003', 'bld-001', 'dt-004', 'FA_FIELD_DEVICE', 'Detector de humo DH-01', 'Detector fotoeléctrico corredor norte', 4001, '2024-05-20', '2027-05-20', 1, NULL, '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
('dev-005', 'zone-005', 'loc-004', 'bld-001', 'dt-005', 'SPRINKLER_DEVICE', 'Extintor ABC 6kg', 'Extintor polvo químico oficinas', 5001, '2024-06-01', '2025-06-01', 1, NULL, '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
('dev-006', 'zone-006', 'loc-005', 'bld-002', 'dt-006', 'SPRINKLER_DEVICE', 'Rociador cabeza 1', 'Rociador tipo pendiente nave A', 6001, '2024-04-15', '2029-04-15', 1, NULL, '2025-01-15T09:00:00Z', '2025-01-15T09:00:00Z'),
('dev-007', 'zone-007', 'loc-006', 'bld-002', 'dt-002', 'JOCKEY_PUMP', 'Jockey Pump B', 'Bomba mantenimiento depósito', 2002, '2024-07-01', '2025-07-01', 1, NULL, '2025-01-15T09:00:00Z', '2025-01-15T09:00:00Z'),
('dev-008', 'zone-008', 'loc-007', 'bld-003', 'dt-001', 'FACP_DEVICE', 'Panel quirófanos', 'Panel de alarma bloque quirúrgico', 1002, '2024-08-10', '2026-08-10', 1, NULL, '2025-01-20T10:00:00Z', '2025-01-20T10:00:00Z'),
('dev-009', 'zone-009', 'loc-008', 'bld-003', 'dt-004', 'FA_FIELD_DEVICE', 'Detector pasillo 101', 'Detector área internación', 4002, '2024-09-01', '2027-09-01', 1, NULL, '2025-01-20T10:00:00Z', '2025-01-20T10:00:00Z'),
('dev-010', 'zone-001', 'loc-001', 'bld-001', 'dt-007', 'FA_FIELD_DEVICE', 'Desfibrilador DEA-01', 'Desfibrilador automático sala emergencias', 7001, '2024-02-01', '2027-02-01', 1, NULL, '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
('dev-015', 'zone-006', 'loc-005', 'bld-002', 'dt-006', 'SPRINKLER_DEVICE', 'Rociador cabeza 2', 'Rociador tipo montante nave A', 6002, '2024-04-15', '2029-04-15', 1, NULL, '2025-01-15T09:00:00Z', '2025-01-15T09:00:00Z'),
('dev-016', 'zone-008', 'loc-007', 'bld-003', 'dt-008', 'FA_FIELD_DEVICE', 'Monitor anestesia Q1', 'Monitor gases anestésicos quirófano 1', 7003, '2024-09-01', '2028-09-01', 1, NULL, '2025-01-20T10:00:00Z', '2025-01-20T10:00:00Z'),
('dev-017', 'zone-009', 'loc-008', 'bld-003', 'dt-005', 'SPRINKLER_DEVICE', 'Extintor pasillo internación', 'Extintor ABC 4kg pasillo central', 5003, '2024-10-01', '2025-10-01', 1, NULL, '2025-01-20T10:00:00Z', '2025-01-20T10:00:00Z');

-- Inspections (buildingId y locationId son refs lógicas)
-- Solo 3 inspecciones demo: PENDING (bld-001), IN_PROGRESS (bld-002), DONE_COMPLETED (bld-003)
INSERT INTO inspections (id, buildingId, locationId, type, status, scheduledDate, approvalDate, result, notes, signer, signed, signDate, startedAt, inspectionReportId, inspectionTemplateId, coverPageId, createdByEmail, createdAt, updatedAt) VALUES
('insp-001', 'bld-001', NULL, 'Weekly', 'PENDING', '2025-03-20T08:00:00Z', NULL, NULL, 'Inspección semanal - Hospital Central (programada)', NULL, 0, NULL, NULL, NULL, 'tpl-001', NULL, 'inspector@example.com', '2025-03-10T09:00:00Z', '2025-03-10T09:00:00Z'),
('insp-002', 'bld-002', NULL, 'Monthly', 'IN_PROGRESS', '2025-03-18T10:00:00Z', NULL, NULL, 'Inspección mensual depósito - en curso', NULL, 0, NULL, '2025-03-18T09:00:00Z', NULL, 'tpl-002', NULL, 'inspector@example.com', '2025-03-15T09:00:00Z', '2025-03-18T09:00:00Z'),
('insp-003', 'bld-003', NULL, 'Weekly', 'DONE_COMPLETED', '2025-03-15T09:00:00Z', '2025-03-15T16:00:00Z', 'SUCCESS', 'Inspección semanal Clínica Sur - completada OK', 'María García', 1, '2025-03-15T16:00:00Z', '2025-03-15T09:00:00Z', 'rpt-001', 'tpl-002', NULL, 'inspector@example.com', '2025-03-12T09:00:00Z', '2025-03-15T16:00:00Z');

-- Inspection assignments (FK: inspectionId -> inspections)
INSERT INTO inspection_assignments (id, inspectionId, userEmail, role, createdAt) VALUES
('asgn-001', 'insp-001', 'inspector@example.com', 'INSPECTOR', '2025-03-10T09:00:00Z'),
('asgn-003', 'insp-002', 'inspector@example.com', 'INSPECTOR', '2025-03-15T09:00:00Z'),
('asgn-005', 'insp-003', 'inspector@example.com', 'INSPECTOR', '2025-03-12T09:00:00Z');

-- Photos (referenciadas por observations.mediaId)
INSERT INTO photos (id, mediaUrl, name, description, fileDetailsJson, localPath, timestamp, inspectorId, stepId, deviceId, createdAt) VALUES
('photo-001', 'https://example.com/media/photo1.jpg', 'Evidencia_rociador.jpg', 'Foto rociador alineado', '{"size": 245000}', NULL, '2025-03-18T09:15:00Z', 'usr-001', 'step-008', 'dev-006', '2025-03-18T09:15:00Z'),
('photo-002', 'https://example.com/media/photo2.jpg', 'Evidencia_Jockey.jpg', 'Foto Jockey Pump presión baja', '{"size": 312000}', NULL, '2025-03-18T09:30:00Z', 'usr-001', 'step-009', 'dev-007', '2025-03-18T09:30:00Z');

-- Tests (FK: deviceId -> devices, inspectionId -> inspections)
-- insp-001 PENDING (bld-001): mayoría PENDING, 1 COMPLETED para variedad
-- insp-002 IN_PROGRESS (bld-002): mezcla PENDING, COMPLETED, FAILED
-- insp-003 DONE_COMPLETED (bld-003): todos COMPLETED
INSERT INTO tests (id, deviceId, inspectionId, testTemplateId, testStepIds, name, description, status, applicable, createdAt, updatedAt) VALUES
('test-001', 'dev-001', 'insp-001', 'tt-001', '["step-001","step-002"]', 'Verificación FACP', 'Revisión del panel de alarma', 'PENDING', 1, '2025-03-10T09:00:00Z', '2025-03-10T09:00:00Z'),
('test-002', 'dev-002', 'insp-001', 'tt-002', '["step-003"]', 'Prueba Jockey Pump', 'Verificación de presión', 'PENDING', 1, '2025-03-10T09:00:00Z', '2025-03-10T09:00:00Z'),
('test-003', 'dev-004', 'insp-001', 'tt-003', '["step-004"]', 'Prueba detector humo', 'Verificación detector DH-01', 'PENDING', 1, '2025-03-10T09:00:00Z', '2025-03-10T09:00:00Z'),
('test-004', 'dev-005', 'insp-001', 'tt-006', '["step-005"]', 'Inspección extintor ABC', 'Verificar sello y manómetro', 'COMPLETED', 1, '2025-03-10T09:00:00Z', '2025-03-10T10:00:00Z'),
('test-005', 'dev-010', 'insp-001', 'tt-004', '["step-006","step-007"]', 'Prueba desfibrilador', 'Verificación de carga y electrodos', 'PENDING', 1, '2025-03-10T09:00:00Z', '2025-03-10T09:00:00Z'),
('test-006', 'dev-006', 'insp-002', 'tt-012', '["step-008"]', 'Prueba rociador cabeza 1', 'Verificar alineación y obstrucción', 'COMPLETED', 1, '2025-03-18T09:00:00Z', '2025-03-18T09:45:00Z'),
('test-007', 'dev-007', 'insp-002', 'tt-002', '["step-009"]', 'Prueba Jockey Pump B', 'Verificación de presión depósito', 'FAILED', 1, '2025-03-18T09:00:00Z', '2025-03-18T10:00:00Z'),
('test-008', 'dev-015', 'insp-002', 'tt-012', '["step-010"]', 'Prueba rociador cabeza 2', 'Verificar alineación nave A', 'PENDING', 1, '2025-03-18T09:00:00Z', '2025-03-18T09:00:00Z'),
('test-009', 'dev-008', 'insp-003', 'tt-001', '["step-011","step-012"]', 'Verificación panel quirófanos', 'Revisión del panel de alarma', 'COMPLETED', 1, '2025-03-15T09:00:00Z', '2025-03-15T16:00:00Z'),
('test-010', 'dev-009', 'insp-003', 'tt-003', '["step-013"]', 'Prueba detector pasillo 101', 'Verificación detector humo', 'COMPLETED', 1, '2025-03-15T09:00:00Z', '2025-03-15T14:00:00Z'),
('test-011', 'dev-016', 'insp-003', 'tt-011', '["step-014"]', 'Calibración monitor anestesia', 'Verificar sensor gases anestésicos', 'COMPLETED', 1, '2025-03-15T09:00:00Z', '2025-03-15T15:00:00Z'),
('test-012', 'dev-017', 'insp-003', 'tt-006', '["step-015"]', 'Inspección extintor pasillo', 'Verificar sello y accesibilidad', 'COMPLETED', 1, '2025-03-15T09:00:00Z', '2025-03-15T15:30:00Z'),
('test-013', 'dev-003', 'insp-001', 'tt-008', '["step-016","step-017"]', 'Prueba Fire Pump', 'Verificación de arranque y presión nominal', 'PENDING', 1, '2025-03-10T09:00:00Z', '2025-03-10T09:00:00Z');

-- Steps (FK: testId -> tests)
INSERT INTO steps (id, testId, name, testStepType, applicable, status, description, valueJson, minValue, maxValue, createdAt, updatedAt) VALUES
('step-001', 'test-001', 'Lectura de display', 'SIMPLE_VALUE', 1, 'PENDING', 'Verificar lectura en display principal', NULL, NULL, NULL, '2025-03-10T09:00:00Z', '2025-03-10T09:00:00Z'),
('step-002', 'test-001', 'Alarma en verde', 'BINARY', 1, 'PENDING', 'Indicador de alarma en estado normal', NULL, NULL, NULL, '2025-03-10T09:00:00Z', '2025-03-10T09:00:00Z'),
('step-003', 'test-002', 'Presión (psi)', 'RANGE', 1, 'PENDING', 'Medir presión en salida', NULL, 50.0, 120.0, '2025-03-10T09:00:00Z', '2025-03-10T09:00:00Z'),
('step-004', 'test-003', 'Prueba de humo', 'BINARY', 1, 'PENDING', 'Simular humo y verificar alarma', NULL, NULL, NULL, '2025-03-10T09:00:00Z', '2025-03-10T09:00:00Z'),
('step-005', 'test-004', 'Sello intacto', 'BINARY', 1, 'COMPLETED', 'Verificar que el sello está intacto', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-10T09:00:00Z', '2025-03-10T10:00:00Z'),
('step-006', 'test-005', 'Nivel de carga batería', 'RANGE', 1, 'PENDING', 'Verificar carga suficiente', NULL, 80.0, 100.0, '2025-03-10T09:00:00Z', '2025-03-10T09:00:00Z'),
('step-007', 'test-005', 'Estado electrodos', 'BINARY', 1, 'PENDING', 'Electrodos no vencidos', NULL, NULL, NULL, '2025-03-10T09:00:00Z', '2025-03-10T09:00:00Z'),
('step-008', 'test-006', 'Alineación rociador', 'BINARY', 1, 'COMPLETED', 'Rociador alineado correctamente', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-18T09:00:00Z', '2025-03-18T09:45:00Z'),
('step-009', 'test-007', 'Presión (psi)', 'RANGE', 1, 'FAILED', 'Presión fuera de rango - falla', '{"value": 35.0, "valueType": "NUMERIC_UNIT_VALUE"}', 50.0, 120.0, '2025-03-18T09:00:00Z', '2025-03-18T10:00:00Z'),
('step-010', 'test-008', 'Alineación rociador', 'BINARY', 1, 'PENDING', 'Verificar alineación', NULL, NULL, NULL, '2025-03-18T09:00:00Z', '2025-03-18T09:00:00Z'),
('step-011', 'test-009', 'Display operativo', 'BINARY', 1, 'COMPLETED', 'Display del panel OK', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-15T09:00:00Z', '2025-03-15T16:00:00Z'),
('step-012', 'test-009', 'Sin alarmas activas', 'BINARY', 1, 'COMPLETED', 'No hay alarmas pendientes', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-15T09:00:00Z', '2025-03-15T16:00:00Z'),
('step-013', 'test-010', 'Prueba de humo', 'BINARY', 1, 'COMPLETED', 'Detector respondió correctamente', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-15T09:00:00Z', '2025-03-15T14:00:00Z'),
('step-014', 'test-011', 'Lectura sensor gases', 'RANGE', 1, 'COMPLETED', 'Calibración OK', '{"value": 2.1, "valueType": "NUMERIC_UNIT_VALUE"}', 0.0, 5.0, '2025-03-15T09:00:00Z', '2025-03-15T15:00:00Z'),
('step-015', 'test-012', 'Sello y accesibilidad', 'BINARY', 1, 'COMPLETED', 'Extintor accesible y sello intacto', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-15T09:00:00Z', '2025-03-15T15:30:00Z'),
('step-016', 'test-013', 'Presión nominal (psi)', 'RANGE', 1, 'PENDING', 'Verificar presión de descarga', NULL, 100.0, 150.0, '2025-03-10T09:00:00Z', '2025-03-10T09:00:00Z'),
('step-017', 'test-013', 'Arranque correcto', 'BINARY', 1, 'PENDING', 'Bomba arranca sin fallos', NULL, NULL, NULL, '2025-03-10T09:00:00Z', '2025-03-10T09:00:00Z');

-- Observations (FK: testStepId -> steps, inspectionId -> inspections; mediaId -> photos)
INSERT INTO observations (id, testStepId, inspectionId, name, type, description, deficiencyTypeId, mediaId, createdAt, updatedAt) VALUES
('obs-001', 'step-008', 'insp-002', 'Evidencia visual', 'REMARKS', 'Rociador alineado correctamente', NULL, 'photo-001', '2025-03-18T09:15:00Z', '2025-03-18T09:15:00Z'),
('obs-002', 'step-009', 'insp-002', 'Deficiencia detectada', 'DEFICIENCY', 'Presión baja - requiere revisión de bomba', 'def-001', 'photo-002', '2025-03-18T10:00:00Z', '2025-03-18T10:00:00Z');

-- Audit logs (userId references users)
INSERT INTO audit_logs (id, userId, entityType, entityId, action, metadataJson, createdAt) VALUES
('log-001', 'usr-001', 'Inspection', 'insp-001', 'CREATE', '{"ip": "192.168.1.10"}', '2025-03-10T09:00:00Z'),
('log-002', 'usr-001', 'Inspection', 'insp-002', 'START', '{"startedAt": "2025-03-18T09:00:00Z"}', '2025-03-18T09:00:00Z'),
('log-003', 'usr-001', 'Inspection', 'insp-003', 'SIGN', '{"signer": "María García"}', '2025-03-15T16:00:00Z');

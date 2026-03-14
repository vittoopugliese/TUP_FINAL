-- ============================================================
-- Datos de ejemplo para Inspections Android app
-- Tablas: users, locations, zones, devices, inspections,
--         inspection_assignments, photos, tests, steps,
--         observations, audit_logs
-- Orden respeta dependencias de foreign keys
--
-- Backend: Este archivo está adaptado en backend/src/main/resources/data.sql (H2/snake_case)
-- ============================================================

-- Users (no FKs)
INSERT INTO users (id, email, firstName, lastName, avatarImage, phoneNumber, role, lastLoginAt, createdAt) VALUES
('usr-001', 'inspector@example.com', 'María', 'García', 'https://example.com/avatars/1.jpg', '+54 11 1234-5678', 'INSPECTOR', '2025-03-14T10:00:00Z', '2025-01-15T09:00:00Z'),
('usr-002', 'operator@example.com', 'Carlos', 'López', NULL, '+54 11 8765-4321', 'OPERATOR', '2025-03-13T14:30:00Z', '2025-01-20T11:00:00Z'),
('usr-003', 'admin@inspections.com', 'Admin', 'Sistema', NULL, '+54 11 5555-0000', 'ADMIN', '2025-03-14T08:00:00Z', '2025-01-01T00:00:00Z'),
('usr-004', 'juan.perez@empresa.com', 'Juan', 'Pérez', NULL, '+54 11 3333-4444', 'INSPECTOR', '2025-03-12T16:00:00Z', '2025-02-01T10:00:00Z'),
('usr-005', 'ana.martinez@empresa.com', 'Ana', 'Martínez', NULL, '+54 11 2222-1111', 'OPERATOR', '2025-03-11T09:00:00Z', '2025-02-05T14:00:00Z');

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
INSERT INTO devices (id, zoneId, locationId, buildingId, manufacturerId, modelId, deviceTypeId, deviceCategory, name, description, deviceSerialNumber, installationDate, expirationDate, enabled, attributeIds, createdAt, updatedAt) VALUES
('dev-001', 'zone-002', 'loc-001', 'bld-001', 'mfr-001', 'mod-001', 'dt-001', 'FACP_DEVICE', 'Panel de alarma principal', 'Panel de control FACP - Sala emergencias', 1001, '2024-01-15', '2026-01-15', 1, NULL, '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
('dev-002', 'zone-003', 'loc-002', 'bld-001', 'mfr-002', 'mod-002', 'dt-002', 'JOCKEY_PUMP', 'Jockey Pump A', 'Bomba de mantenimiento de presión', 2001, '2024-03-01', '2025-09-01', 1, NULL, '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
('dev-003', 'zone-003', 'loc-002', 'bld-001', 'mfr-002', 'mod-003', 'dt-003', 'FIRE_PUMP', 'Fire Pump principal', 'Bomba principal contra incendios', 3001, '2024-02-10', '2026-02-10', 1, NULL, '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
('dev-004', 'zone-004', 'loc-003', 'bld-001', 'mfr-003', 'mod-004', 'dt-004', 'FA_FIELD_DEVICE', 'Detector de humo DH-01', 'Detector fotoeléctrico corredor norte', 4001, '2024-05-20', '2027-05-20', 1, NULL, '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
('dev-005', 'zone-005', 'loc-004', 'bld-001', 'mfr-004', 'mod-005', 'dt-005', 'SPRINKLER_DEVICE', 'Extintor ABC 6kg', 'Extintor polvo químico oficinas', 5001, '2024-06-01', '2025-06-01', 1, NULL, '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
('dev-006', 'zone-006', 'loc-005', 'bld-002', 'mfr-005', 'mod-006', 'dt-006', 'SPRINKLER_DEVICE', 'Rociador cabeza 1', 'Rociador tipo pendiente nave A', 6001, '2024-04-15', '2029-04-15', 1, NULL, '2025-01-15T09:00:00Z', '2025-01-15T09:00:00Z'),
('dev-007', 'zone-007', 'loc-006', 'bld-002', 'mfr-002', 'mod-002', 'dt-002', 'JOCKEY_PUMP', 'Jockey Pump B', 'Bomba mantenimiento depósito', 2002, '2024-07-01', '2025-07-01', 1, NULL, '2025-01-15T09:00:00Z', '2025-01-15T09:00:00Z'),
('dev-008', 'zone-008', 'loc-007', 'bld-003', 'mfr-001', 'mod-007', 'dt-001', 'FACP_DEVICE', 'Panel quirófanos', 'Panel de alarma bloque quirúrgico', 1002, '2024-08-10', '2026-08-10', 1, NULL, '2025-01-20T10:00:00Z', '2025-01-20T10:00:00Z'),
('dev-009', 'zone-009', 'loc-008', 'bld-003', 'mfr-003', 'mod-004', 'dt-004', 'FA_FIELD_DEVICE', 'Detector pasillo 101', 'Detector área internación', 4002, '2024-09-01', '2027-09-01', 1, NULL, '2025-01-20T10:00:00Z', '2025-01-20T10:00:00Z');

-- Inspections (buildingId y locationId son refs lógicas)
INSERT INTO inspections (id, buildingId, locationId, type, status, scheduledDate, approvalDate, result, notes, signer, signed, signDate, startedAt, inspectionReportId, inspectionTemplateId, coverPageId, createdAt, updatedAt) VALUES
('insp-001', 'bld-001', 'loc-001', 'Weekly', 'IN_PROGRESS', '2025-03-15T08:00:00Z', NULL, NULL, 'Inspección semanal - Sala emergencias', NULL, 0, NULL, '2025-03-15T08:00:00Z', NULL, 'tpl-001', NULL, '2025-03-10T09:00:00Z', '2025-03-15T08:00:00Z'),
('insp-002', 'bld-001', 'loc-002', 'Monthly', 'DONE_COMPLETED', '2025-03-01T09:00:00Z', '2025-03-01T16:00:00Z', 'SUCCESS', 'Inspección mensual - Sala bombas OK', 'María García', 1, '2025-03-01T16:00:00Z', '2025-03-01T09:00:00Z', 'rpt-001', 'tpl-002', NULL, '2025-02-25T09:00:00Z', '2025-03-01T16:00:00Z'),
('insp-003', 'bld-001', 'loc-003', 'Weekly', 'PENDING', '2025-03-18T10:00:00Z', NULL, NULL, 'Pendiente - Corredor Norte', NULL, 0, NULL, NULL, NULL, 'tpl-001', NULL, '2025-03-12T09:00:00Z', '2025-03-12T09:00:00Z'),
('insp-004', 'bld-002', 'loc-005', 'Monthly', 'PENDING', '2025-03-20T08:00:00Z', NULL, NULL, 'Inspección mensual depósito', NULL, 0, NULL, NULL, NULL, 'tpl-002', NULL, '2025-03-10T09:00:00Z', '2025-03-10T09:00:00Z'),
('insp-005', 'bld-002', 'loc-006', 'Annually', 'PENDING', '2025-04-01T09:00:00Z', NULL, NULL, 'Inspección anual - Sala máquinas', NULL, 0, NULL, NULL, NULL, 'tpl-003', NULL, '2025-03-01T09:00:00Z', '2025-03-01T09:00:00Z'),
('insp-006', 'bld-003', 'loc-007', 'Daily', 'IN_PROGRESS', '2025-03-14T07:00:00Z', NULL, NULL, 'Inspección diaria quirófanos', NULL, 0, NULL, '2025-03-14T07:00:00Z', NULL, 'tpl-001', NULL, '2025-03-14T06:00:00Z', '2025-03-14T07:00:00Z'),
('insp-007', 'bld-003', 'loc-008', 'Weekly', 'DONE_FAILED', '2025-03-10T09:00:00Z', '2025-03-10T14:00:00Z', 'FAILED', 'Falló detector 101 - requiere reemplazo', 'Juan Pérez', 1, '2025-03-10T14:00:00Z', '2025-03-10T09:00:00Z', 'rpt-002', 'tpl-002', NULL, '2025-03-05T09:00:00Z', '2025-03-10T14:00:00Z');

-- Inspection assignments (FK: inspectionId -> inspections)
INSERT INTO inspection_assignments (id, inspectionId, userEmail, role, createdAt) VALUES
('asgn-001', 'insp-001', 'inspector@example.com', 'INSPECTOR', '2025-03-10T09:00:00Z'),
('asgn-002', 'insp-001', 'operator@example.com', 'OPERATOR', '2025-03-10T09:00:00Z'),
('asgn-003', 'insp-001', 'ana.martinez@empresa.com', 'OPERATOR', '2025-03-10T09:30:00Z'),
('asgn-004', 'insp-002', 'inspector@example.com', 'INSPECTOR', '2025-02-25T09:00:00Z'),
('asgn-005', 'insp-003', 'juan.perez@empresa.com', 'INSPECTOR', '2025-03-12T09:00:00Z'),
('asgn-006', 'insp-004', 'inspector@example.com', 'INSPECTOR', '2025-03-10T09:00:00Z'),
('asgn-007', 'insp-005', 'juan.perez@empresa.com', 'INSPECTOR', '2025-03-01T09:00:00Z'),
('asgn-008', 'insp-006', 'inspector@example.com', 'INSPECTOR', '2025-03-14T06:00:00Z'),
('asgn-009', 'insp-006', 'operator@example.com', 'OPERATOR', '2025-03-14T06:00:00Z'),
('asgn-010', 'insp-007', 'juan.perez@empresa.com', 'INSPECTOR', '2025-03-05T09:00:00Z');

-- Photos (referenciadas por observations.mediaId)
INSERT INTO photos (id, mediaUrl, name, description, fileDetailsJson, localPath, timestamp, inspectorId, stepId, deviceId, createdAt) VALUES
('photo-001', 'https://example.com/media/photo1.jpg', 'Evidencia_FACP.jpg', 'Foto del panel de alarma', '{"size": 245000}', NULL, '2025-03-15T08:15:00Z', 'usr-001', 'step-001', 'dev-001', '2025-03-15T08:15:00Z'),
('photo-002', 'https://example.com/media/photo2.jpg', 'Evidencia_Jockey.jpg', 'Foto de la jockey pump', '{"size": 312000}', NULL, '2025-03-15T08:30:00Z', 'usr-001', 'step-002', 'dev-002', '2025-03-15T08:30:00Z'),
('photo-003', 'https://example.com/media/photo3.jpg', 'Detector_falla.jpg', 'Detector 101 con falla', '{"size": 189000}', NULL, '2025-03-10T14:00:00Z', 'usr-004', 'step-004', 'dev-009', '2025-03-10T14:00:00Z');

-- Tests (FK: deviceId -> devices, inspectionId -> inspections)
INSERT INTO tests (id, deviceId, inspectionId, testTemplateId, testStepIds, name, description, status, applicable, createdAt, updatedAt) VALUES
('test-001', 'dev-001', 'insp-001', 'tt-001', '["step-001","step-002"]', 'Verificación FACP', 'Revisión del panel de alarma', 'PENDING', 1, '2025-03-15T08:00:00Z', '2025-03-15T08:00:00Z'),
('test-002', 'dev-002', 'insp-001', 'tt-002', '["step-003"]', 'Prueba Jockey Pump', 'Verificación de presión', 'PENDING', 1, '2025-03-15T08:00:00Z', '2025-03-15T08:30:00Z'),
('test-003', 'dev-001', 'insp-002', 'tt-001', '["step-004"]', 'Verificación FACP', 'Revisión mensual completada', 'COMPLETED', 1, '2025-03-01T09:00:00Z', '2025-03-01T16:00:00Z'),
('test-004', 'dev-004', 'insp-003', 'tt-003', '["step-005"]', 'Prueba detector', 'Verificación detector humo', 'PENDING', 1, '2025-03-12T09:00:00Z', '2025-03-12T09:00:00Z'),
('test-005', 'dev-009', 'insp-007', 'tt-003', '["step-006"]', 'Prueba detector 101', 'Detector falló - reemplazo requerido', 'FAILED', 1, '2025-03-10T09:00:00Z', '2025-03-10T14:00:00Z');

-- Steps (FK: testId -> tests)
INSERT INTO steps (id, testId, name, testStepType, applicable, status, description, valueJson, minValue, maxValue, createdAt, updatedAt) VALUES
('step-001', 'test-001', 'Lectura de display', 'SIMPLE_VALUE', 1, 'PENDING', 'Verificar lectura en display principal', NULL, NULL, NULL, '2025-03-15T08:00:00Z', '2025-03-15T08:00:00Z'),
('step-002', 'test-001', 'Alarma en verde', 'BINARY', 1, 'SUCCESS', 'Indicador de alarma en estado normal', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-15T08:00:00Z', '2025-03-15T08:10:00Z'),
('step-003', 'test-002', 'Presión (psi)', 'RANGE', 1, 'PENDING', 'Medir presión en salida', NULL, 50.0, 120.0, '2025-03-15T08:00:00Z', '2025-03-15T08:00:00Z'),
('step-004', 'test-003', 'Verificación general', 'BINARY', 1, 'SUCCESS', 'Inspección visual OK', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-01T09:00:00Z', '2025-03-01T16:00:00Z'),
('step-005', 'test-004', 'Prueba de humo', 'BINARY', 1, 'PENDING', 'Simular humo y verificar alarma', NULL, NULL, NULL, '2025-03-12T09:00:00Z', '2025-03-12T09:00:00Z'),
('step-006', 'test-005', 'Prueba de humo', 'BINARY', 1, 'FAILED', 'Detector no respondió - falla', '{"value": false, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-10T09:00:00Z', '2025-03-10T14:00:00Z');

-- Observations (FK: testStepId -> steps, inspectionId -> inspections; mediaId -> photos)
INSERT INTO observations (id, testStepId, inspectionId, name, type, description, deficiencyTypeId, mediaId, createdAt, updatedAt) VALUES
('obs-001', 'step-001', 'insp-001', 'Evidencia visual', 'REMARKS', 'Panel limpio y sin fallas', NULL, 'photo-001', '2025-03-15T08:15:00Z', '2025-03-15T08:15:00Z'),
('obs-002', 'step-002', 'insp-001', 'Nota adicional', 'RECOMMENDATIONS', 'Revisar batería de respaldo en próxima inspección', NULL, NULL, '2025-03-15T08:20:00Z', '2025-03-15T08:20:00Z'),
('obs-003', 'step-006', 'insp-007', 'Deficiencia detectada', 'DEFICIENCY', 'Detector 101 no responde - requiere reemplazo inmediato', 'def-001', 'photo-003', '2025-03-10T14:00:00Z', '2025-03-10T14:00:00Z');

-- Audit logs (userId references users)
INSERT INTO audit_logs (id, userId, entityType, entityId, action, metadataJson, createdAt) VALUES
('log-001', 'usr-001', 'Inspection', 'insp-001', 'CREATE', '{"ip": "192.168.1.10"}', '2025-03-15T08:00:00Z'),
('log-002', 'usr-001', 'Test', 'test-002', 'UPDATE', '{"status": "PENDING"}', '2025-03-15T08:30:00Z'),
('log-003', 'usr-001', 'Inspection', 'insp-002', 'SIGN', '{"signer": "María García"}', '2025-03-01T16:00:00Z'),
('log-004', 'usr-004', 'Inspection', 'insp-007', 'SIGN', '{"signer": "Juan Pérez", "result": "FAILED"}', '2025-03-10T14:00:00Z'),
('log-005', 'usr-001', 'Inspection', 'insp-006', 'START', '{"startedAt": "2025-03-14T07:00:00Z"}', '2025-03-14T07:00:00Z');

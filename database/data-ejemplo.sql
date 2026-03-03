-- Sample/test data for Inspections Android app
-- Tables: users, locations, zones, devices, inspections, photos, tests, steps, observations, audit_logs
-- Order respects foreign key dependencies

-- Users (no FKs)
INSERT INTO users (id, email, firstName, lastName, avatarImage, phoneNumber, role, lastLoginAt, createdAt) VALUES
('usr-001', 'inspector@example.com', 'María', 'García', 'https://example.com/avatars/1.jpg', '+54 11 1234-5678', 'INSPECTOR', '2025-02-28T10:00:00Z', '2025-01-15T09:00:00Z'),
('usr-002', 'operator@example.com', 'Carlos', 'López', NULL, '+54 11 8765-4321', 'OPERATOR', '2025-02-27T14:30:00Z', '2025-01-20T11:00:00Z');

-- Locations (buildingId is string ref)
INSERT INTO locations (id, buildingId, name, details, createdAt, updatedAt) VALUES
('loc-001', 'bld-001', 'Sala de emergencias', 'Sala principal de emergencias', '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
('loc-002', 'bld-001', 'Sala de bombas', 'Sala de equipos contra incendios', '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z');

-- Zones (FK: locationId -> locations)
INSERT INTO zones (id, locationId, name, details) VALUES
('zone-001', 'loc-001', 'Equipo médico', 'Desfibriladores y equipos de primeros auxilios'),
('zone-002', 'loc-001', 'Sistemas de seguridad', 'Panel de alarma y extintores'),
('zone-003', 'loc-002', 'Bombas contra incendios', 'Jockey pump y fire pump');

-- Devices (FK: zoneId -> zones, locationId -> locations)
INSERT INTO devices (id, zoneId, locationId, buildingId, manufacturerId, modelId, deviceTypeId, deviceCategory, name, description, deviceSerialNumber, installationDate, expirationDate, enabled, attributeIds, createdAt, updatedAt) VALUES
('dev-001', 'zone-002', 'loc-001', 'bld-001', 'mfr-001', 'mod-001', 'dt-001', 'FACP_DEVICE', 'Panel de alarma principal', 'Panel de control FACP', 1001, '2024-01-15', '2026-01-15', 1, NULL, '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
('dev-002', 'zone-003', 'loc-002', 'bld-001', 'mfr-002', 'mod-002', 'dt-002', 'JOCKEY_PUMP', 'Jockey Pump A', 'Bomba de mantenimiento de presión', 2001, '2024-03-01', '2025-09-01', 1, NULL, '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z'),
('dev-003', 'zone-003', 'loc-002', 'bld-001', 'mfr-002', 'mod-003', 'dt-003', 'FIRE_PUMP', 'Fire Pump principal', 'Bomba principal contra incendios', 3001, '2024-02-10', '2026-02-10', 1, NULL, '2025-01-10T08:00:00Z', '2025-01-10T08:00:00Z');

-- Inspections (buildingId is string ref)
INSERT INTO inspections (id, buildingId, type, status, scheduledDate, approvalDate, result, notes, signer, signed, signDate, startedAt, inspectionReportId, inspectionTemplateId, coverPageId, createdAt, updatedAt) VALUES
('insp-001', 'bld-001', 'Weekly', 'IN_PROGRESS', '2025-03-01', NULL, NULL, 'Inspección semanal programada', NULL, 0, NULL, '2025-03-01T08:00:00Z', NULL, 'tpl-001', NULL, '2025-02-28T09:00:00Z', '2025-03-01T08:00:00Z'),
('insp-002', 'bld-001', 'Monthly', 'DONE_COMPLETED', '2025-02-15', '2025-02-15T16:00:00Z', 'SUCCESS', 'Inspección mensual completada', 'María García', 1, '2025-02-15T16:00:00Z', '2025-02-15T09:00:00Z', 'rpt-001', 'tpl-002', NULL, '2025-02-10T09:00:00Z', '2025-02-15T16:00:00Z');

-- Photos (standalone; referenced by observations.mediaId)
INSERT INTO photos (id, mediaUrl, name, description, fileDetailsJson, localPath, timestamp, inspectorId, stepId, deviceId, createdAt) VALUES
('photo-001', 'https://example.com/media/photo1.jpg', 'Evidencia_FACP.jpg', 'Foto del panel de alarma', '{"size": 245000}', NULL, '2025-03-01T08:15:00Z', 'usr-001', 'step-001', 'dev-001', '2025-03-01T08:15:00Z'),
('photo-002', 'https://example.com/media/photo2.jpg', 'Evidencia_Jockey.jpg', 'Foto de la jockey pump', '{"size": 312000}', NULL, '2025-03-01T08:30:00Z', 'usr-001', 'step-002', 'dev-002', '2025-03-01T08:30:00Z');

-- Tests (FK: deviceId -> devices, inspectionId -> inspections)
INSERT INTO tests (id, deviceId, inspectionId, testTemplateId, testStepIds, name, description, status, applicable, createdAt, updatedAt) VALUES
('test-001', 'dev-001', 'insp-001', 'tt-001', '["step-001","step-002"]', 'Verificación FACP', 'Revisión del panel de alarma', 'PENDING', 1, '2025-03-01T08:00:00Z', '2025-03-01T08:00:00Z'),
('test-002', 'dev-002', 'insp-001', 'tt-002', '["step-003"]', 'Prueba Jockey Pump', 'Verificación de presión', 'PENDING', 1, '2025-03-01T08:00:00Z', '2025-03-01T08:30:00Z'),
('test-003', 'dev-001', 'insp-002', 'tt-001', '["step-004"]', 'Verificación FACP', 'Revisión mensual completada', 'COMPLETED', 1, '2025-02-15T09:00:00Z', '2025-02-15T16:00:00Z');

-- Steps (FK: testId -> tests)
INSERT INTO steps (id, testId, name, testStepType, applicable, status, description, valueJson, minValue, maxValue, createdAt, updatedAt) VALUES
('step-001', 'test-001', 'Lectura de display', 'SIMPLE_VALUE', 1, 'PENDING', 'Verificar lectura en display principal', NULL, NULL, NULL, '2025-03-01T08:00:00Z', '2025-03-01T08:00:00Z'),
('step-002', 'test-001', 'Alarma en verde', 'BINARY', 1, 'SUCCESS', 'Indicador de alarma en estado normal', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-01T08:00:00Z', '2025-03-01T08:10:00Z'),
('step-003', 'test-002', 'Presión (psi)', 'RANGE', 1, 'PENDING', 'Medir presión en salida', NULL, 50.0, 120.0, '2025-03-01T08:00:00Z', '2025-03-01T08:00:00Z'),
('step-004', 'test-003', 'Verificación general', 'BINARY', 1, 'SUCCESS', 'Inspección visual OK', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-02-15T09:00:00Z', '2025-02-15T16:00:00Z');

-- Observations (FK: testStepId -> steps, inspectionId -> inspections; mediaId -> photos)
INSERT INTO observations (id, testStepId, inspectionId, name, type, description, deficiencyTypeId, mediaId, createdAt, updatedAt) VALUES
('obs-001', 'step-001', 'insp-001', 'Evidencia visual', 'REMARKS', 'Panel limpio y sin fallas', NULL, 'photo-001', '2025-03-01T08:15:00Z', '2025-03-01T08:15:00Z'),
('obs-002', 'step-002', 'insp-001', 'Nota adicional', 'RECOMMENDATIONS', 'Revisar batería de respaldo en próxima inspección', NULL, NULL, '2025-03-01T08:20:00Z', '2025-03-01T08:20:00Z');

-- Audit logs (FK: userId -> users)
INSERT INTO audit_logs (id, userId, entityType, entityId, action, metadataJson, createdAt) VALUES
('log-001', 'usr-001', 'Inspection', 'insp-001', 'CREATE', '{"ip": "192.168.1.10"}', '2025-03-01T08:00:00Z'),
('log-002', 'usr-001', 'Test', 'test-002', 'UPDATE', '{"status": "PENDING"}', '2025-03-01T08:30:00Z'),
('log-003', 'usr-001', 'Inspection', 'insp-002', 'SIGN', '{"signer": "María García"}', '2025-02-15T16:00:00Z');

-- ============================================================
-- Datos de ejemplo para Inspections API backend
-- Adaptado de database/data-ejemplo.sql para H2 (snake_case)
-- El usuario admin se crea en DataInitializer (admin@inspections.com)
-- ============================================================

-- Locations (building_id es ref lógica)
INSERT INTO locations (id, building_id, name, details, created_at, updated_at) VALUES
('loc-001', 'bld-001', 'Sala de emergencias', 'Sala principal de emergencias', '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('loc-002', 'bld-001', 'Sala de bombas', 'Sala de equipos contra incendios', '2025-01-10 08:00:00', '2025-01-10 08:00:00');

-- Zones (FK: location_id -> locations)
INSERT INTO zones (id, location_id, name, details) VALUES
('zone-001', 'loc-001', 'Equipo médico', 'Desfibriladores y equipos de primeros auxilios'),
('zone-002', 'loc-001', 'Sistemas de seguridad', 'Panel de alarma y extintores'),
('zone-003', 'loc-002', 'Bombas contra incendios', 'Jockey pump y fire pump');

-- Devices (FK: zone_id -> zones, location_id -> locations)
INSERT INTO devices (id, zone_id, location_id, building_id, manufacturer_id, model_id, device_type_id, device_category, name, description, device_serial_number, installation_date, expiration_date, enabled, attribute_ids, created_at, updated_at) VALUES
('dev-001', 'zone-002', 'loc-001', 'bld-001', 'mfr-001', 'mod-001', 'dt-001', 'FACP_DEVICE', 'Panel de alarma principal', 'Panel de control FACP', 1001, '2024-01-15', '2026-01-15', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('dev-002', 'zone-003', 'loc-002', 'bld-001', 'mfr-002', 'mod-002', 'dt-002', 'JOCKEY_PUMP', 'Jockey Pump A', 'Bomba de mantenimiento de presión', 2001, '2024-03-01', '2025-09-01', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('dev-003', 'zone-003', 'loc-002', 'bld-001', 'mfr-002', 'mod-003', 'dt-003', 'FIRE_PUMP', 'Fire Pump principal', 'Bomba principal contra incendios', 3001, '2024-02-10', '2026-02-10', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00');

-- Inspections (building_id es ref lógica)
INSERT INTO inspections (id, building_id, type, status, scheduled_date, approval_date, result, notes, signer, signed, sign_date, started_at, inspection_report_id, inspection_template_id, cover_page_id, created_at, updated_at) VALUES
('insp-001', 'bld-001', 'Weekly', 'IN_PROGRESS', '2025-03-01 08:00:00', NULL, NULL, 'Inspección semanal programada', NULL, FALSE, NULL, '2025-03-01 08:00:00', NULL, 'tpl-001', NULL, '2025-02-28 09:00:00', '2025-03-01 08:00:00'),
('insp-002', 'bld-001', 'Monthly', 'DONE_COMPLETED', '2025-02-15 09:00:00', '2025-02-15 16:00:00', 'SUCCESS', 'Inspección mensual completada', 'María García', TRUE, '2025-02-15 16:00:00', '2025-02-15 09:00:00', 'rpt-001', 'tpl-002', NULL, '2025-02-10 09:00:00', '2025-02-15 16:00:00');

-- Photos
INSERT INTO photos (id, media_url, name, description, file_details_json, local_path, timestamp, inspector_id, step_id, device_id, created_at) VALUES
('photo-001', 'https://example.com/media/photo1.jpg', 'Evidencia_FACP.jpg', 'Foto del panel de alarma', '{"size": 245000}', NULL, '2025-03-01 08:15:00', 'admin-001', 'step-001', 'dev-001', '2025-03-01 08:15:00'),
('photo-002', 'https://example.com/media/photo2.jpg', 'Evidencia_Jockey.jpg', 'Foto de la jockey pump', '{"size": 312000}', NULL, '2025-03-01 08:30:00', 'admin-001', 'step-002', 'dev-002', '2025-03-01 08:30:00');

-- Tests (FK: device_id -> devices, inspection_id -> inspections)
INSERT INTO tests (id, device_id, inspection_id, test_template_id, test_step_ids, name, description, status, applicable, created_at, updated_at) VALUES
('test-001', 'dev-001', 'insp-001', 'tt-001', '["step-001","step-002"]', 'Verificación FACP', 'Revisión del panel de alarma', 'PENDING', TRUE, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
('test-002', 'dev-002', 'insp-001', 'tt-002', '["step-003"]', 'Prueba Jockey Pump', 'Verificación de presión', 'PENDING', TRUE, '2025-03-01 08:00:00', '2025-03-01 08:30:00'),
('test-003', 'dev-001', 'insp-002', 'tt-001', '["step-004"]', 'Verificación FACP', 'Revisión mensual completada', 'COMPLETED', TRUE, '2025-02-15 09:00:00', '2025-02-15 16:00:00');

-- Steps (FK: test_id -> tests)
INSERT INTO steps (id, test_id, name, test_step_type, applicable, status, description, value_json, min_value, max_value, created_at, updated_at) VALUES
('step-001', 'test-001', 'Lectura de display', 'SIMPLE_VALUE', TRUE, 'PENDING', 'Verificar lectura en display principal', NULL, NULL, NULL, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
('step-002', 'test-001', 'Alarma en verde', 'BINARY', TRUE, 'SUCCESS', 'Indicador de alarma en estado normal', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-01 08:00:00', '2025-03-01 08:10:00'),
('step-003', 'test-002', 'Presión (psi)', 'RANGE', TRUE, 'PENDING', 'Medir presión en salida', NULL, 50.0, 120.0, '2025-03-01 08:00:00', '2025-03-01 08:00:00'),
('step-004', 'test-003', 'Verificación general', 'BINARY', TRUE, 'SUCCESS', 'Inspección visual OK', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-02-15 09:00:00', '2025-02-15 16:00:00');

-- Observations (FK: test_step_id -> steps, inspection_id -> inspections)
INSERT INTO observations (id, test_step_id, inspection_id, name, type, description, deficiency_type_id, media_id, created_at, updated_at) VALUES
('obs-001', 'step-001', 'insp-001', 'Evidencia visual', 'REMARKS', 'Panel limpio y sin fallas', NULL, 'photo-001', '2025-03-01 08:15:00', '2025-03-01 08:15:00'),
('obs-002', 'step-002', 'insp-001', 'Nota adicional', 'RECOMMENDATIONS', 'Revisar batería de respaldo en próxima inspección', NULL, NULL, '2025-03-01 08:20:00', '2025-03-01 08:20:00');

-- Audit logs (user_id referencia admin creado en DataInitializer)
INSERT INTO audit_logs (id, user_id, entity_type, entity_id, action, metadata_json, created_at) VALUES
('log-001', 'admin-001', 'Inspection', 'insp-001', 'CREATE', '{"ip": "192.168.1.10"}', '2025-03-01 08:00:00'),
('log-002', 'admin-001', 'Test', 'test-002', 'UPDATE', '{"status": "PENDING"}', '2025-03-01 08:30:00'),
('log-003', 'admin-001', 'Inspection', 'insp-002', 'SIGN', '{"signer": "María García"}', '2025-02-15 16:00:00');

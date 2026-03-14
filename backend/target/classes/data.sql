-- ============================================================
-- Datos de ejemplo para Inspections API backend
-- Adaptado de database/data-ejemplo.sql para H2 (snake_case)
-- El usuario admin se crea en DataInitializer (admin@inspections.com)
-- ============================================================

-- Locations (building_id es ref lógica)
INSERT INTO locations (id, building_id, name, details, created_at, updated_at) VALUES
('loc-001', 'bld-001', 'Sala de emergencias', 'Sala principal de emergencias - Planta Baja', '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('loc-002', 'bld-001', 'Sala de bombas', 'Sala de equipos contra incendios - Sótano 1', '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('loc-003', 'bld-001', 'Piso 1 - Corredor Norte', 'Corredor principal con detectores de humo', '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('loc-004', 'bld-001', 'Piso 2 - Oficinas', 'Área de oficinas administrativas', '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('loc-005', 'bld-002', 'Nave principal', 'Nave de almacenamiento - zona A', '2025-01-15 09:00:00', '2025-01-15 09:00:00'),
('loc-006', 'bld-002', 'Sala de máquinas', 'Equipos HVAC y bombas', '2025-01-15 09:00:00', '2025-01-15 09:00:00'),
('loc-007', 'bld-003', 'Quirófanos', 'Bloque quirúrgico - 3 salas', '2025-01-20 10:00:00', '2025-01-20 10:00:00'),
('loc-008', 'bld-003', 'Área de internación', 'Habitaciones 101-120', '2025-01-20 10:00:00', '2025-01-20 10:00:00');

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
('zone-009', 'loc-008', 'Pasillo central', 'Detectores y extintores');

-- Devices (FK: zone_id -> zones, location_id -> locations)
INSERT INTO devices (id, zone_id, location_id, building_id, manufacturer_id, model_id, device_type_id, device_category, name, description, device_serial_number, installation_date, expiration_date, enabled, attribute_ids, created_at, updated_at) VALUES
('dev-001', 'zone-002', 'loc-001', 'bld-001', 'mfr-001', 'mod-001', 'dt-001', 'FACP_DEVICE', 'Panel de alarma principal', 'Panel de control FACP - Sala emergencias', 1001, '2024-01-15', '2026-01-15', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('dev-002', 'zone-003', 'loc-002', 'bld-001', 'mfr-002', 'mod-002', 'dt-002', 'JOCKEY_PUMP', 'Jockey Pump A', 'Bomba de mantenimiento de presión', 2001, '2024-03-01', '2025-09-01', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('dev-003', 'zone-003', 'loc-002', 'bld-001', 'mfr-002', 'mod-003', 'dt-003', 'FIRE_PUMP', 'Fire Pump principal', 'Bomba principal contra incendios', 3001, '2024-02-10', '2026-02-10', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('dev-004', 'zone-004', 'loc-003', 'bld-001', 'mfr-003', 'mod-004', 'dt-004', 'FA_FIELD_DEVICE', 'Detector de humo DH-01', 'Detector fotoeléctrico corredor norte', 4001, '2024-05-20', '2027-05-20', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('dev-005', 'zone-005', 'loc-004', 'bld-001', 'mfr-004', 'mod-005', 'dt-005', 'SPRINKLER_DEVICE', 'Extintor ABC 6kg', 'Extintor polvo químico oficinas', 5001, '2024-06-01', '2025-06-01', TRUE, NULL, '2025-01-10 08:00:00', '2025-01-10 08:00:00'),
('dev-006', 'zone-006', 'loc-005', 'bld-002', 'mfr-005', 'mod-006', 'dt-006', 'SPRINKLER_DEVICE', 'Rociador cabeza 1', 'Rociador tipo pendiente nave A', 6001, '2024-04-15', '2029-04-15', TRUE, NULL, '2025-01-15 09:00:00', '2025-01-15 09:00:00'),
('dev-007', 'zone-007', 'loc-006', 'bld-002', 'mfr-002', 'mod-002', 'dt-002', 'JOCKEY_PUMP', 'Jockey Pump B', 'Bomba mantenimiento depósito', 2002, '2024-07-01', '2025-07-01', TRUE, NULL, '2025-01-15 09:00:00', '2025-01-15 09:00:00'),
('dev-008', 'zone-008', 'loc-007', 'bld-003', 'mfr-001', 'mod-007', 'dt-001', 'FACP_DEVICE', 'Panel quirófanos', 'Panel de alarma bloque quirúrgico', 1002, '2024-08-10', '2026-08-10', TRUE, NULL, '2025-01-20 10:00:00', '2025-01-20 10:00:00'),
('dev-009', 'zone-009', 'loc-008', 'bld-003', 'mfr-003', 'mod-004', 'dt-004', 'FA_FIELD_DEVICE', 'Detector pasillo 101', 'Detector área internación', 4002, '2024-09-01', '2027-09-01', TRUE, NULL, '2025-01-20 10:00:00', '2025-01-20 10:00:00');

-- Inspections (building_id y location_id son refs lógicas)
INSERT INTO inspections (id, building_id, location_id, type, status, scheduled_date, approval_date, result, notes, signer, signed, sign_date, started_at, inspection_report_id, inspection_template_id, cover_page_id, created_at, updated_at) VALUES
('insp-001', 'bld-001', 'loc-001', 'Weekly', 'IN_PROGRESS', '2025-03-15 08:00:00', NULL, NULL, 'Inspección semanal - Sala emergencias', NULL, FALSE, NULL, '2025-03-15 08:00:00', NULL, 'tpl-001', NULL, '2025-03-10 09:00:00', '2025-03-15 08:00:00'),
('insp-002', 'bld-001', 'loc-002', 'Monthly', 'DONE_COMPLETED', '2025-03-01 09:00:00', '2025-03-01 16:00:00', 'SUCCESS', 'Inspección mensual - Sala bombas OK', 'María García', TRUE, '2025-03-01 16:00:00', '2025-03-01 09:00:00', 'rpt-001', 'tpl-002', NULL, '2025-02-25 09:00:00', '2025-03-01 16:00:00'),
('insp-003', 'bld-001', 'loc-003', 'Weekly', 'PENDING', '2025-03-18 10:00:00', NULL, NULL, 'Pendiente - Corredor Norte', NULL, FALSE, NULL, NULL, NULL, 'tpl-001', NULL, '2025-03-12 09:00:00', '2025-03-12 09:00:00'),
('insp-004', 'bld-002', 'loc-005', 'Monthly', 'PENDING', '2025-03-20 08:00:00', NULL, NULL, 'Inspección mensual depósito', NULL, FALSE, NULL, NULL, NULL, 'tpl-002', NULL, '2025-03-10 09:00:00', '2025-03-10 09:00:00'),
('insp-005', 'bld-002', 'loc-006', 'Annually', 'PENDING', '2025-04-01 09:00:00', NULL, NULL, 'Inspección anual - Sala máquinas', NULL, FALSE, NULL, NULL, NULL, 'tpl-003', NULL, '2025-03-01 09:00:00', '2025-03-01 09:00:00'),
('insp-006', 'bld-003', 'loc-007', 'Daily', 'IN_PROGRESS', '2025-03-14 07:00:00', NULL, NULL, 'Inspección diaria quirófanos', NULL, FALSE, NULL, '2025-03-14 07:00:00', NULL, 'tpl-001', NULL, '2025-03-14 06:00:00', '2025-03-14 07:00:00'),
('insp-007', 'bld-003', 'loc-008', 'Weekly', 'DONE_FAILED', '2025-03-10 09:00:00', '2025-03-10 14:00:00', 'FAILED', 'Falló detector 101 - requiere reemplazo', 'Juan Pérez', TRUE, '2025-03-10 14:00:00', '2025-03-10 09:00:00', 'rpt-002', 'tpl-002', NULL, '2025-03-05 09:00:00', '2025-03-10 14:00:00');

-- Inspection assignments (FK: inspection_id -> inspections)
INSERT INTO inspection_assignments (id, inspection_id, user_email, role, created_at) VALUES
('asgn-001', 'insp-001', 'inspector@example.com', 'INSPECTOR', '2025-03-10 09:00:00'),
('asgn-002', 'insp-001', 'operator@example.com', 'OPERATOR', '2025-03-10 09:00:00'),
('asgn-003', 'insp-001', 'ana.martinez@empresa.com', 'OPERATOR', '2025-03-10 09:30:00'),
('asgn-004', 'insp-002', 'inspector@example.com', 'INSPECTOR', '2025-02-25 09:00:00'),
('asgn-005', 'insp-003', 'juan.perez@empresa.com', 'INSPECTOR', '2025-03-12 09:00:00'),
('asgn-006', 'insp-004', 'inspector@example.com', 'INSPECTOR', '2025-03-10 09:00:00'),
('asgn-007', 'insp-005', 'juan.perez@empresa.com', 'INSPECTOR', '2025-03-01 09:00:00'),
('asgn-008', 'insp-006', 'inspector@example.com', 'INSPECTOR', '2025-03-14 06:00:00'),
('asgn-009', 'insp-006', 'operator@example.com', 'OPERATOR', '2025-03-14 06:00:00'),
('asgn-010', 'insp-007', 'juan.perez@empresa.com', 'INSPECTOR', '2025-03-05 09:00:00');

-- Photos
INSERT INTO photos (id, media_url, name, description, file_details_json, local_path, timestamp, inspector_id, step_id, device_id, created_at) VALUES
('photo-001', 'https://example.com/media/photo1.jpg', 'Evidencia_FACP.jpg', 'Foto del panel de alarma', '{"size": 245000}', NULL, '2025-03-15 08:15:00', 'admin-001', 'step-001', 'dev-001', '2025-03-15 08:15:00'),
('photo-002', 'https://example.com/media/photo2.jpg', 'Evidencia_Jockey.jpg', 'Foto de la jockey pump', '{"size": 312000}', NULL, '2025-03-15 08:30:00', 'admin-001', 'step-002', 'dev-002', '2025-03-15 08:30:00'),
('photo-003', 'https://example.com/media/photo3.jpg', 'Detector_falla.jpg', 'Detector 101 con falla', '{"size": 189000}', NULL, '2025-03-10 14:00:00', 'admin-001', 'step-004', 'dev-009', '2025-03-10 14:00:00');

-- Tests (FK: device_id -> devices, inspection_id -> inspections)
INSERT INTO tests (id, device_id, inspection_id, test_template_id, test_step_ids, name, description, status, applicable, created_at, updated_at) VALUES
('test-001', 'dev-001', 'insp-001', 'tt-001', '["step-001","step-002"]', 'Verificación FACP', 'Revisión del panel de alarma', 'PENDING', TRUE, '2025-03-15 08:00:00', '2025-03-15 08:00:00'),
('test-002', 'dev-002', 'insp-001', 'tt-002', '["step-003"]', 'Prueba Jockey Pump', 'Verificación de presión', 'PENDING', TRUE, '2025-03-15 08:00:00', '2025-03-15 08:30:00'),
('test-003', 'dev-001', 'insp-002', 'tt-001', '["step-004"]', 'Verificación FACP', 'Revisión mensual completada', 'COMPLETED', TRUE, '2025-03-01 09:00:00', '2025-03-01 16:00:00'),
('test-004', 'dev-004', 'insp-003', 'tt-003', '["step-005"]', 'Prueba detector', 'Verificación detector humo', 'PENDING', TRUE, '2025-03-12 09:00:00', '2025-03-12 09:00:00'),
('test-005', 'dev-009', 'insp-007', 'tt-003', '["step-006"]', 'Prueba detector 101', 'Detector falló - reemplazo requerido', 'FAILED', TRUE, '2025-03-10 09:00:00', '2025-03-10 14:00:00');

-- Steps (FK: test_id -> tests)
INSERT INTO steps (id, test_id, name, test_step_type, applicable, status, description, value_json, min_value, max_value, created_at, updated_at) VALUES
('step-001', 'test-001', 'Lectura de display', 'SIMPLE_VALUE', TRUE, 'PENDING', 'Verificar lectura en display principal', NULL, NULL, NULL, '2025-03-15 08:00:00', '2025-03-15 08:00:00'),
('step-002', 'test-001', 'Alarma en verde', 'BINARY', TRUE, 'SUCCESS', 'Indicador de alarma en estado normal', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-15 08:00:00', '2025-03-15 08:10:00'),
('step-003', 'test-002', 'Presión (psi)', 'RANGE', TRUE, 'PENDING', 'Medir presión en salida', NULL, 50.0, 120.0, '2025-03-15 08:00:00', '2025-03-15 08:00:00'),
('step-004', 'test-003', 'Verificación general', 'BINARY', TRUE, 'SUCCESS', 'Inspección visual OK', '{"value": true, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-01 09:00:00', '2025-03-01 16:00:00'),
('step-005', 'test-004', 'Prueba de humo', 'BINARY', TRUE, 'PENDING', 'Simular humo y verificar alarma', NULL, NULL, NULL, '2025-03-12 09:00:00', '2025-03-12 09:00:00'),
('step-006', 'test-005', 'Prueba de humo', 'BINARY', TRUE, 'FAILED', 'Detector no respondió - falla', '{"value": false, "valueType": "BOOLEAN_VALUE"}', NULL, NULL, '2025-03-10 09:00:00', '2025-03-10 14:00:00');

-- Observations (FK: test_step_id -> steps, inspection_id -> inspections)
INSERT INTO observations (id, test_step_id, inspection_id, name, type, description, deficiency_type_id, media_id, created_at, updated_at) VALUES
('obs-001', 'step-001', 'insp-001', 'Evidencia visual', 'REMARKS', 'Panel limpio y sin fallas', NULL, 'photo-001', '2025-03-15 08:15:00', '2025-03-15 08:15:00'),
('obs-002', 'step-002', 'insp-001', 'Nota adicional', 'RECOMMENDATIONS', 'Revisar batería de respaldo en próxima inspección', NULL, NULL, '2025-03-15 08:20:00', '2025-03-15 08:20:00'),
('obs-003', 'step-006', 'insp-007', 'Deficiencia detectada', 'DEFICIENCY', 'Detector 101 no responde - requiere reemplazo inmediato', 'def-001', 'photo-003', '2025-03-10 14:00:00', '2025-03-10 14:00:00');

-- Audit logs (user_id referencia admin creado en DataInitializer)
INSERT INTO audit_logs (id, user_id, entity_type, entity_id, action, metadata_json, created_at) VALUES
('log-001', 'admin-001', 'Inspection', 'insp-001', 'CREATE', '{"ip": "192.168.1.10"}', '2025-03-15 08:00:00'),
('log-002', 'admin-001', 'Test', 'test-002', 'UPDATE', '{"status": "PENDING"}', '2025-03-15 08:30:00'),
('log-003', 'admin-001', 'Inspection', 'insp-002', 'SIGN', '{"signer": "María García"}', '2025-03-01 16:00:00'),
('log-004', 'admin-001', 'Inspection', 'insp-007', 'SIGN', '{"signer": "Juan Pérez", "result": "FAILED"}', '2025-03-10 14:00:00'),
('log-005', 'admin-001', 'Inspection', 'insp-006', 'START', '{"startedAt": "2025-03-14T07:00:00Z"}', '2025-03-14 07:00:00');

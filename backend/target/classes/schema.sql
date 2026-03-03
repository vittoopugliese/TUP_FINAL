-- ============================================================
-- H2-compatible schema for Inspections API backend
-- Based on database/schema.sql (Room/SQLite reference)
-- Adapted for H2 in-memory database with backend-specific fields
-- ============================================================

-- Users (backend adds passwordHash and enabled for authentication)
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    email VARCHAR(255),
    password_hash VARCHAR(255),
    first_name VARCHAR(255),
    last_name VARCHAR(255),
    avatar_image VARCHAR(500),
    phone_number VARCHAR(50),
    role VARCHAR(50),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_user_email ON users(email);

-- Locations (buildingId is logical ref, no table)
CREATE TABLE IF NOT EXISTS locations (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    building_id VARCHAR(36),
    name VARCHAR(255),
    details TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_location_building ON locations(building_id);
CREATE INDEX IF NOT EXISTS idx_location_name ON locations(name);

-- Inspections (buildingId is logical ref)
CREATE TABLE IF NOT EXISTS inspections (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    building_id VARCHAR(36),
    type VARCHAR(50),
    status VARCHAR(50),
    scheduled_date TIMESTAMP,
    approval_date TIMESTAMP,
    result VARCHAR(50),
    notes TEXT,
    signer VARCHAR(255),
    signed BOOLEAN NOT NULL DEFAULT FALSE,
    sign_date TIMESTAMP,
    started_at TIMESTAMP,
    inspection_report_id VARCHAR(36),
    inspection_template_id VARCHAR(36),
    cover_page_id VARCHAR(36),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_inspection_building ON inspections(building_id);
CREATE INDEX IF NOT EXISTS idx_inspection_status ON inspections(status);
CREATE INDEX IF NOT EXISTS idx_inspection_date ON inspections(scheduled_date);

-- Photos (no FKs enforced)
CREATE TABLE IF NOT EXISTS photos (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    media_url VARCHAR(500),
    name VARCHAR(255),
    description TEXT,
    file_details_json TEXT,
    local_path VARCHAR(500),
    timestamp TIMESTAMP,
    inspector_id VARCHAR(36),
    step_id VARCHAR(36),
    device_id VARCHAR(36),
    created_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_photo_media_url ON photos(media_url);
CREATE INDEX IF NOT EXISTS idx_photo_step ON photos(step_id);
CREATE INDEX IF NOT EXISTS idx_photo_device ON photos(device_id);

-- Zones (FK: locationId -> locations)
CREATE TABLE IF NOT EXISTS zones (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    location_id VARCHAR(36) NOT NULL,
    name VARCHAR(255),
    details TEXT
);
CREATE INDEX IF NOT EXISTS idx_zone_location ON zones(location_id);

-- Devices (FK: zoneId -> zones, locationId -> locations)
CREATE TABLE IF NOT EXISTS devices (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    zone_id VARCHAR(36) NOT NULL,
    location_id VARCHAR(36) NOT NULL,
    building_id VARCHAR(36),
    manufacturer_id VARCHAR(36),
    model_id VARCHAR(36),
    device_type_id VARCHAR(36),
    device_category VARCHAR(100),
    name VARCHAR(255),
    description TEXT,
    device_serial_number INT,
    installation_date TIMESTAMP,
    expiration_date TIMESTAMP,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    attribute_ids TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_device_zone ON devices(zone_id);
CREATE INDEX IF NOT EXISTS idx_device_location ON devices(location_id);
CREATE INDEX IF NOT EXISTS idx_device_building ON devices(building_id);
CREATE INDEX IF NOT EXISTS idx_device_category ON devices(device_category);

-- Tests (FK: deviceId -> devices, inspectionId -> inspections)
CREATE TABLE IF NOT EXISTS tests (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    device_id VARCHAR(36) NOT NULL,
    inspection_id VARCHAR(36) NOT NULL,
    test_template_id VARCHAR(36),
    test_step_ids TEXT,
    name VARCHAR(255),
    description TEXT,
    status VARCHAR(50),
    applicable BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_test_device ON tests(device_id);
CREATE INDEX IF NOT EXISTS idx_test_inspection ON tests(inspection_id);
CREATE INDEX IF NOT EXISTS idx_test_status ON tests(status);

-- Steps (FK: testId -> tests)
CREATE TABLE IF NOT EXISTS steps (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    test_id VARCHAR(36) NOT NULL,
    name VARCHAR(255),
    test_step_type VARCHAR(50),
    applicable BOOLEAN NOT NULL DEFAULT TRUE,
    status VARCHAR(50),
    description TEXT,
    value_json TEXT,
    min_value DOUBLE,
    max_value DOUBLE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_step_test ON steps(test_id);
CREATE INDEX IF NOT EXISTS idx_step_status ON steps(status);

-- Observations (FK: testStepId -> steps)
CREATE TABLE IF NOT EXISTS observations (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    test_step_id VARCHAR(36) NOT NULL,
    inspection_id VARCHAR(36),
    name VARCHAR(255),
    type VARCHAR(50),
    description TEXT,
    deficiency_type_id VARCHAR(36),
    media_id VARCHAR(36),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_obs_step ON observations(test_step_id);
CREATE INDEX IF NOT EXISTS idx_obs_inspection ON observations(inspection_id);
CREATE INDEX IF NOT EXISTS idx_obs_type ON observations(type);

-- Audit logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id VARCHAR(36) PRIMARY KEY NOT NULL,
    user_id VARCHAR(36),
    entity_type VARCHAR(100),
    entity_id VARCHAR(36),
    action VARCHAR(50),
    metadata_json TEXT,
    created_at TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_audit_user ON audit_logs(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_entity_type ON audit_logs(entity_type);
CREATE INDEX IF NOT EXISTS idx_audit_entity_id ON audit_logs(entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_created ON audit_logs(created_at);

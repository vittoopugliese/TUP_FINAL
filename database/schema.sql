-- DDL for Inspections Android app (Room entities)
-- Reference schema for backend use. Consistent with Room annotations.
-- SQLite compatible.

-- Users (no FKs)
CREATE TABLE users (
    id TEXT PRIMARY KEY NOT NULL,
    email TEXT,
    firstName TEXT,
    lastName TEXT,
    avatarImage TEXT,
    phoneNumber TEXT,
    role TEXT,
    lastLoginAt TEXT,
    createdAt TEXT
);
CREATE INDEX index_users_email ON users(email);

-- Locations (buildingId is logical ref, no table)
CREATE TABLE locations (
    id TEXT PRIMARY KEY NOT NULL,
    buildingId TEXT,
    name TEXT,
    details TEXT,
    createdAt TEXT,
    updatedAt TEXT
);
CREATE INDEX index_locations_buildingId ON locations(buildingId);
CREATE INDEX index_locations_name ON locations(name);

-- Inspections (buildingId is logical ref)
CREATE TABLE inspections (
    id TEXT PRIMARY KEY NOT NULL,
    buildingId TEXT,
    type TEXT,
    status TEXT,
    scheduledDate TEXT,
    approvalDate TEXT,
    result TEXT,
    notes TEXT,
    signer TEXT,
    signed INTEGER NOT NULL DEFAULT 0,
    signDate TEXT,
    startedAt TEXT,
    inspectionReportId TEXT,
    inspectionTemplateId TEXT,
    coverPageId TEXT,
    createdAt TEXT,
    updatedAt TEXT
);
CREATE INDEX index_inspections_buildingId ON inspections(buildingId);
CREATE INDEX index_inspections_status ON inspections(status);
CREATE INDEX index_inspections_scheduledDate ON inspections(scheduledDate);

-- Photos (no FKs enforced by Room)
CREATE TABLE photos (
    id TEXT PRIMARY KEY NOT NULL,
    mediaUrl TEXT,
    name TEXT,
    description TEXT,
    fileDetailsJson TEXT,
    localPath TEXT,
    timestamp TEXT,
    inspectorId TEXT,
    stepId TEXT,
    deviceId TEXT,
    createdAt TEXT
);
CREATE INDEX index_photos_mediaUrl ON photos(mediaUrl);

-- Zones (FK: locationId -> locations)
CREATE TABLE zones (
    id TEXT PRIMARY KEY NOT NULL,
    locationId TEXT NOT NULL,
    name TEXT,
    details TEXT,
    FOREIGN KEY (locationId) REFERENCES locations(id) ON DELETE CASCADE
);
CREATE INDEX index_zones_locationId ON zones(locationId);

-- Devices (FK: zoneId -> zones, locationId -> locations)
CREATE TABLE devices (
    id TEXT PRIMARY KEY NOT NULL,
    zoneId TEXT NOT NULL,
    locationId TEXT NOT NULL,
    buildingId TEXT,
    manufacturerId TEXT,
    modelId TEXT,
    deviceTypeId TEXT,
    deviceCategory TEXT,
    name TEXT,
    description TEXT,
    deviceSerialNumber INTEGER,
    installationDate TEXT,
    expirationDate TEXT,
    enabled INTEGER NOT NULL DEFAULT 1,
    attributeIds TEXT,
    createdAt TEXT,
    updatedAt TEXT,
    FOREIGN KEY (zoneId) REFERENCES zones(id) ON DELETE CASCADE,
    FOREIGN KEY (locationId) REFERENCES locations(id) ON DELETE CASCADE
);
CREATE INDEX index_devices_zoneId ON devices(zoneId);
CREATE INDEX index_devices_locationId ON devices(locationId);
CREATE INDEX index_devices_buildingId ON devices(buildingId);
CREATE INDEX index_devices_deviceCategory ON devices(deviceCategory);

-- Tests (FK: deviceId -> devices, inspectionId -> inspections)
CREATE TABLE tests (
    id TEXT PRIMARY KEY NOT NULL,
    deviceId TEXT NOT NULL,
    inspectionId TEXT NOT NULL,
    testTemplateId TEXT,
    testStepIds TEXT,
    name TEXT,
    description TEXT,
    status TEXT,
    applicable INTEGER NOT NULL DEFAULT 1,
    createdAt TEXT,
    updatedAt TEXT,
    FOREIGN KEY (deviceId) REFERENCES devices(id) ON DELETE CASCADE,
    FOREIGN KEY (inspectionId) REFERENCES inspections(id) ON DELETE CASCADE
);
CREATE INDEX index_tests_deviceId ON tests(deviceId);
CREATE INDEX index_tests_inspectionId ON tests(inspectionId);
CREATE INDEX index_tests_status ON tests(status);

-- Steps (FK: testId -> tests)
CREATE TABLE steps (
    id TEXT PRIMARY KEY NOT NULL,
    testId TEXT NOT NULL,
    name TEXT,
    testStepType TEXT,
    applicable INTEGER NOT NULL DEFAULT 1,
    status TEXT,
    description TEXT,
    valueJson TEXT,
    minValue REAL,
    maxValue REAL,
    createdAt TEXT,
    updatedAt TEXT,
    FOREIGN KEY (testId) REFERENCES tests(id) ON DELETE CASCADE
);
CREATE INDEX index_steps_testId ON steps(testId);
CREATE INDEX index_steps_status ON steps(status);

-- Observations (FK: testStepId -> steps; inspectionId is indexed but not FK in Room)
CREATE TABLE observations (
    id TEXT PRIMARY KEY NOT NULL,
    testStepId TEXT NOT NULL,
    inspectionId TEXT,
    name TEXT,
    type TEXT,
    description TEXT,
    deficiencyTypeId TEXT,
    mediaId TEXT,
    createdAt TEXT,
    updatedAt TEXT,
    FOREIGN KEY (testStepId) REFERENCES steps(id) ON DELETE CASCADE
);
CREATE INDEX index_observations_testStepId ON observations(testStepId);
CREATE INDEX index_observations_inspectionId ON observations(inspectionId);
CREATE INDEX index_observations_type ON observations(type);

-- Audit logs (userId references users, not enforced as FK in Room)
CREATE TABLE audit_logs (
    id TEXT PRIMARY KEY NOT NULL,
    userId TEXT,
    entityType TEXT,
    entityId TEXT,
    action TEXT,
    metadataJson TEXT,
    createdAt TEXT
);
CREATE INDEX index_audit_logs_userId ON audit_logs(userId);
CREATE INDEX index_audit_logs_entityType ON audit_logs(entityType);
CREATE INDEX index_audit_logs_entityId ON audit_logs(entityId);
CREATE INDEX index_audit_logs_createdAt ON audit_logs(createdAt);

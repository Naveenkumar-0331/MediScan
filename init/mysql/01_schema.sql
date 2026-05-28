-- ============================================================
--  MediScan AI — MySQL Schema
--  Auto-runs on first container start via docker-entrypoint.d
-- ============================================================

USE mediscan_db;

-- ----------------------------------------------------------
-- Roles table
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS roles (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        ENUM('ROLE_DOCTOR', 'ROLE_PATIENT', 'ROLE_ADMIN') NOT NULL UNIQUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ----------------------------------------------------------
-- Users table
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name    VARCHAR(100) NOT NULL,
    email        VARCHAR(150) NOT NULL UNIQUE,
    password     VARCHAR(255) NOT NULL,           -- bcrypt hash
    phone        VARCHAR(20),
    is_active    BOOLEAN DEFAULT TRUE,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email)
);

-- ----------------------------------------------------------
-- User ↔ Role mapping (many-to-many)
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS user_roles (
    user_id  BIGINT NOT NULL,
    role_id  BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- ----------------------------------------------------------
-- Reports metadata table
-- Actual file lives in MinIO; extracted text lives in MongoDB
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS reports (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id      BIGINT NOT NULL,
    uploaded_by     BIGINT NOT NULL,              -- doctor's user_id
    report_type     ENUM('LAB', 'XRAY', 'MRI', 'PRESCRIPTION', 'OTHER') DEFAULT 'LAB',
    file_name       VARCHAR(255) NOT NULL,
    s3_key          VARCHAR(500) NOT NULL,         -- MinIO object key
    file_size_bytes BIGINT,
    mime_type       VARCHAR(100),
    status          ENUM('PENDING', 'PROCESSING', 'DONE', 'FAILED') DEFAULT 'PENDING',
    mongo_doc_id    VARCHAR(50),                  -- ref to MongoDB extracted_texts collection
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES users(id),
    FOREIGN KEY (uploaded_by) REFERENCES users(id),
    INDEX idx_patient (patient_id),
    INDEX idx_status (status)
);

-- ----------------------------------------------------------
-- Audit logs table
-- Records every access / mutation on patient records
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS audit_logs (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id      BIGINT,                          -- who performed the action
    action       VARCHAR(100) NOT NULL,           -- e.g. REPORT_VIEW, REPORT_UPLOAD
    resource     VARCHAR(100),                    -- e.g. reports/42
    ip_address   VARCHAR(45),
    user_agent   TEXT,
    status_code  INT,
    created_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_user_action (user_id, action),
    INDEX idx_created_at (created_at)
);

-- ----------------------------------------------------------
-- Seed data — default roles and admin user
-- Admin password: Admin@123 (bcrypt hash below)
-- ----------------------------------------------------------
INSERT IGNORE INTO roles (name) VALUES
    ('ROLE_DOCTOR'),
    ('ROLE_PATIENT'),
    ('ROLE_ADMIN');

INSERT IGNORE INTO users (full_name, email, password) VALUES
    ('System Admin', 'admin@mediscan.ai', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj2NJL3zKrYm');

-- Assign ADMIN role to admin user
INSERT IGNORE INTO user_roles (user_id, role_id)
    SELECT u.id, r.id FROM users u, roles r
    WHERE u.email = 'admin@mediscan.ai' AND r.name = 'ROLE_ADMIN';

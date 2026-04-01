CREATE DATABASE IF NOT EXISTS DXP;

USE DXP;

CREATE TABLE DXP_SMS_TEMPLATE (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    TEMPLATE_NAME VARCHAR(255) NOT NULL UNIQUE,
    TEMPLATE_BODY TEXT NOT NULL,
    IS_ACTIVE TINYINT(1) NOT NULL DEFAULT 1,
    DATE_CREATED DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    DATE_UPDATED DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
);
CREATE TABLE DXP_EMAIL_TEMPLATE (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    TEMPLATE_NAME VARCHAR(255) NOT NULL UNIQUE,
    TEMPLATE_BODY TEXT NOT NULL,
    IS_ACTIVE TINYINT(1) NOT NULL DEFAULT 1,
    DATE_CREATED DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    DATE_UPDATED DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE template_master (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 template_id VARCHAR(36) NOT NULL,
                                 template_name VARCHAR(200) NOT NULL,
                                 message_type VARCHAR(20) NOT NULL,
                                 version VARCHAR(10) NOT NULL,
                                 alert_config JSON NOT NULL,
                                 headers JSON NOT NULL,
                                 raw_content JSON NOT NULL,
                                 indexed_content JSON NOT NULL,
                                 param_mapping JSON NOT NULL,
                                 contentHash VARCHAR(255) DEFAULT NULL,
                                 exceptionReason VARCHAR(500) DEFAULT NULL,
                                 isDuplicateallowed BOOLEAN NOT NULL DEFAULT FALSE,
                                 IS_ACTIVE TINYINT(1) NOT NULL DEFAULT 1,
                                 created_by VARCHAR(100) NOT NULL,
                                 modified_by VARCHAR(100) DEFAULT NULL,
                                 created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 modified_date DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP
);


CREATE TABLE TM_TEMPLATE_AUDIT (

    audit_id VARCHAR(36) PRIMARY KEY,   -- UUID

    draft_id VARCHAR(36) NOT NULL,      -- FK to TM_DRAFT_WORK

    template_id VARCHAR(36) NULL,       -- Snapshot at change time

    template_name VARCHAR(200) NOT NULL,

    version VARCHAR(10) NOT NULL,

    mesaageType ENUM('EMAIL', 'SMS', 'BOTH') NOT NULL,

    indexed_content LONGTEXT NULL,      -- Frozen snapshot

    param_mapping JSON NULL,            -- Frozen snapshot

    is_template_active ENUM('DRAFT', 'ACTIVE', 'INACTIVE') NOT NULL,

    changed_by VARCHAR(100) NULL,

    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    change_reason ENUM('CREATED', 'EDITED', 'DEACTIVATED') NOT NULL,

);
COMMIT;

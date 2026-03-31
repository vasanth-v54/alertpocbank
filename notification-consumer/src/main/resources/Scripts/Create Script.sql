CREATE DATABASE IF NOT EXISTS dxp_retail;
USE dxp_retail;
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

CREATE TABLE TM_DRAFT_WORK (

    draft_id VARCHAR(36) PRIMARY KEY,  -- UUID

    template_id VARCHAR(36) NULL,      -- Set after publish

    template_name VARCHAR(200) NOT NULL,

    channel ENUM('EMAIL', 'SMS', 'BOTH') NOT NULL,

    version VARCHAR(10) NOT NULL,

    alert_config JSON NULL,            -- Domain, AlertId, etc.

    email_fields JSON NULL,            -- Subject, CC, BCC

    sms_fields JSON NULL,              -- Mobile number mapping

    raw_content LONGTEXT NULL,         -- Original template content

    indexed_content LONGTEXT NULL,     -- Processed/indexed content

    param_mapping JSON NULL,           -- [{seq,name,augment}]

    is_template_active ENUM('DRAFT', 'ACTIVE', 'INACTIVE') NOT NULL DEFAULT 'DRAFT',

    created_by VARCHAR(100) NULL,
    updated_by VARCHAR(100) NULL,

    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE TM_TEMPLATE_AUDIT (

    audit_id VARCHAR(36) PRIMARY KEY,   -- UUID

    draft_id VARCHAR(36) NOT NULL,      -- FK to TM_DRAFT_WORK

    template_id VARCHAR(36) NULL,       -- Snapshot at change time

    template_name VARCHAR(200) NOT NULL,

    version VARCHAR(10) NOT NULL,

    channel ENUM('EMAIL', 'SMS', 'BOTH') NOT NULL,

    indexed_content LONGTEXT NULL,      -- Frozen snapshot

    param_mapping JSON NULL,            -- Frozen snapshot

    is_template_active ENUM('DRAFT', 'ACTIVE', 'INACTIVE') NOT NULL,

    changed_by VARCHAR(100) NULL,

    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    change_reason ENUM('CREATED', 'EDITED', 'DEACTIVATED') NOT NULL,

    -- Foreign Key Constraint
    CONSTRAINT FK_AUDIT_DRAFT
        FOREIGN KEY (draft_id)
        REFERENCES TM_DRAFT_WORK(draft_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
COMMIT;

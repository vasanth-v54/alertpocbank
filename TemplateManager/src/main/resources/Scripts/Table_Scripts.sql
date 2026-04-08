CREATE DATABASE IF NOT EXISTS DXP;

USE DXP;

CREATE TABLE dxp.DXP_SMS_TEMPLATE (
                                      ID INT AUTO_INCREMENT PRIMARY KEY,
                                      TEMPLATE_NAME VARCHAR(255) NOT NULL UNIQUE,
                                      TEMPLATE_BODY JSON NOT NULL,
                                      IS_ACTIVE BOOLEAN NOT NULL DEFAULT TRUE,
                                      DATE_CREATED DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                      DATE_UPDATED DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE dxp.DXP_EMAIL_TEMPLATE (
                                        ID INT AUTO_INCREMENT PRIMARY KEY,
                                        TEMPLATE_NAME VARCHAR(255) NOT NULL UNIQUE,
                                        TEMPLATE_BODY JSON NOT NULL,
                                        IS_ACTIVE BOOLEAN NOT NULL DEFAULT TRUE,
                                        DATE_CREATED DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                        DATE_UPDATED DATETIME NULL ON UPDATE CURRENT_TIMESTAMP
);

-----Updated template_master table

CREATE TABLE dxp.template_master (
                                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                         template_name VARCHAR(255) NOT NULL,
                                         version VARCHAR(20),
                                         message_type VARCHAR(20),
                                         alert_config JSON,
                                         headers JSON,
                                         raw_content JSON,
                                         indexed_content JSON,
                                         param_mapping JSON,
                                         contentHash VARCHAR(255),
                                         exceptionReason VARCHAR(255),
                                         isDuplicateallowed BOOLEAN DEFAULT FALSE,
                                         is_active VARCHAR(1) DEFAULT '1',
                                         created_by VARCHAR(50),
                                         modified_by VARCHAR(50),
                                         created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                         modified_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                         created_at TIMESTAMP NULL,
                                         updated_at TIMESTAMP NULL,
                                         updated_by VARCHAR(50),
                                         CONSTRAINT uk_template UNIQUE (template_name, message_type, is_active)
);



CREATE TABLE TM_TEMPLATE_AUDIT (

    audit_id VARCHAR(36) PRIMARY KEY,   -- UUID

    draft_id VARCHAR(36) NOT NULL,      -- FK to TM_DRAFT_WORK

    template_id VARCHAR(36) NULL,       -- Snapshot at change time

    template_name VARCHAR(200) NOT NULL,

    version VARCHAR(10) NOT NULL,

    mesaageType ENUM('EMAIL', 'SMS', 'BOTH') NOT NULL,

    indexed_content LONGTEXT NULL,

    param_mapping JSON NULL,

    is_template_active ENUM('DRAFT', 'ACTIVE', 'INACTIVE') NOT NULL,

    changed_by VARCHAR(100) NULL,

    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    change_reason ENUM('CREATED', 'EDITED', 'DEACTIVATED') NOT NULL,

);


COMMIT;

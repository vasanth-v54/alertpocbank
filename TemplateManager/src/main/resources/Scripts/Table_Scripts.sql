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



CREATE TABLE TM_TEMPLATE_AUDIT (

                                   audit_id VARCHAR(36) PRIMARY KEY,   -- UUID

                                   template_id BIGINT NOT NULL,        -- FK to template_master(id)

                                   template_name VARCHAR(200) NOT NULL, -- for readability/logging

                                   version VARCHAR(10) NOT NULL,

                                   channel ENUM('EMAIL', 'SMS', 'BOTH') NOT NULL,

                                   indexed_content LONGTEXT NULL,      -- snapshot of template content

                                   param_mapping JSON NULL,            -- snapshot mapping

                                   is_template_active ENUM('DRAFT', 'ACTIVE', 'INACTIVE') NOT NULL,

                                   changed_by VARCHAR(100) NULL,

                                   changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                   change_reason ENUM('CREATED', 'EDITED', 'DEACTIVATED') NOT NULL,

    -- ✅ Foreign Key Constraint
                                   CONSTRAINT fk_template_id
                                       FOREIGN KEY (template_id)
                                           REFERENCES template_master(id)
                                           ON DELETE CASCADE
                                           ON UPDATE CASCADE
);
COMMIT;

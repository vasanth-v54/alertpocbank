
-- Template identifiers Table 
CREATE TABLE routing_key_config (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    template_identifiers JSON NOT NULL COMMENT 'Stores template keys/identifiers in JSON format',

    is_active BOOLEAN DEFAULT TRUE COMMENT 'Indicates active/inactive configuration',

    config_name VARCHAR(100) NOT NULL COMMENT 'Version or configuration name',

    effective_from TIMESTAMP NOT NULL COMMENT 'When this config becomes effective',

    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    created_by VARCHAR(100) NOT NULL COMMENT 'Created by user/system',

    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Last update time',
    updated_by VARCHAR(100) DEFAULT NULL COMMENT 'Last updated by user/system',

    CONSTRAINT uk_config_name UNIQUE (config_name)
);

-- Account Inactivity(60 Days) - ATM Pensioner

INSERT INTO routing_key_config (
    template_identifiers,
    is_active,
    config_name,
    effective_from,
    created_by,
    updated_by
) VALUES (
    '{
        "alertType":"Account Inactivity(60 Days) - ATM Pensioner",
        "eventType":"ewbIntegrationProject-AcInactivePreNoticeATMPensioner_AA_ACTIVITY_EVENT",
        "originatingSource":"",
        "messageType":"Email",
        "legalCompanyCode":"RB",
        "application":"Accounts"
    }',
    TRUE,
    'v1',
    NOW(),
    'SYSTEM',
    'SYSTEM'
);

INSERT INTO routing_key_config (
    template_identifiers,
    is_active,
    config_name,
    effective_from,
    created_by,
    updated_by
) VALUES (
    JSON_OBJECT(
        'alertType','CUSTOMER_CREATED',
        'eventType','CustomerEvents-CustomerCreated_CU_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','EMAIL',
        'legalCompanyCode','RB',
        'application','CRM.DIGI.CUSTOMER,CUSTOMER-CREATION-SUCCESS'
    ),
    TRUE,
    'v1',
    NOW(),
    'SYSTEM',
    'SYSTEM'
);

INSERT INTO routing_key_config (
    template_identifiers,
    is_active,
    config_name,
    effective_from,
    created_by,
    updated_by
) VALUES (
    JSON_OBJECT(
        'alertType','LOAN_DISBURSEMENT_SUCCESSFUL',
        'eventType','ArrangementEvents-LoanDisbursementSuccessful_AA_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','EMAIL',
        'legalCompanyCode','RB',
        'application','LENDING.DIGI.LOANS,LENDING-DISBURSEMENT-SUCCESS'
    ),
    TRUE,
    'v1',
    NOW(),
    'SYSTEM',
    'SYSTEM'
);


INSERT INTO routing_key_config (
    template_identifiers,
    is_active,
    config_name,
    effective_from,
    created_by,
    updated_by
) VALUES (
    JSON_OBJECT(
        'alertType','LOAN_PAST_DUE_REMINDER',
        'eventType','ArrangementEvents-LoanPastDueReminder_AA_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','EMAIL',
        'legalCompanyCode','RB',
        'application','LENDING.DIGI.LOANS,LENDING-PASTDUE-REMINDER'
    ),
    TRUE,
    'v1',
    NOW(),
    'SYSTEM',
    'SYSTEM'
);

INSERT INTO routing_key_config (
    template_identifiers,
    is_active,
    config_name,
    effective_from,
    created_by,
    updated_by
) VALUES (
    JSON_OBJECT(
        'alertType','FUND_TRANSFER_SUCCESSFUL',
        'eventType','PaymentEvents-FundTransferSuccessful_TPH_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','EMAIL',
        'legalCompanyCode','RB',
        'application','TPH.DIGI.PAYMENTS,PAYMENTS-FUND-TRANSFER-SUCCESS'
    ),
    TRUE,
    'v1',
    NOW(),
    'SYSTEM',
    'SYSTEM'
);

INSERT INTO routing_key_config (
    template_identifiers,
    is_active,
    config_name,
    effective_from,
    created_by,
    updated_by
) VALUES (
    JSON_OBJECT(
        'alertType','LOAN_DISBURSEMENT_SUCCESSFUL',
        'eventType','ArrangementEvents-LoanDisbursementSuccessful_AA_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','SMS',
        'legalCompanyCode','RB',
        'application','LENDING.DIGI.LOANS,LENDING-DISBURSEMENT-SUCCESS'
    ),
    TRUE,
    'v1',
    NOW(),
    'SYSTEM',
    'SYSTEM'
);

INSERT INTO routing_key_config (
    template_identifiers,
    is_active,
    config_name,
    effective_from,
    created_by,
    updated_by
) VALUES (
    JSON_OBJECT(
        'alertType','LOAN_PAST_DUE_REMINDER',
        'eventType','ArrangementEvents-LoanPastDueReminder_AA_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','SMS',
        'legalCompanyCode','RB',
        'application','LENDING.DIGI.LOANS,LENDING-PASTDUE-REMINDER'
    ),
    TRUE,
    'v1',
    NOW(),
    'SYSTEM',
    'SYSTEM'
);

INSERT INTO routing_key_config (
    template_identifiers,
    is_active,
    config_name,
    effective_from,
    created_by,
    updated_by
) VALUES (
    JSON_OBJECT(
        'alertType','FUND_TRANSFER_SUCCESSFUL',
        'eventType','PaymentEvents-FundTransferSuccessful_TPH_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','SMS',
        'legalCompanyCode','RB',
        'application','TPH.DIGI.PAYMENTS,PAYMENTS-FUND-TRANSFER-SUCCESS'
    ),
    TRUE,
    'v1',
    NOW(),
    'SYSTEM',
    'SYSTEM'
);

INSERT INTO routing_key_config (
    template_identifiers,
    is_active,
    config_name,
    effective_from,
    created_by,
    updated_by
) VALUES (
    JSON_OBJECT(
        'alertType','CUSTOMER_CREATED',
        'eventType','CustomerEvents-CustomerCreated_CU_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','SMS',
        'legalCompanyCode','RB',
        'application','CRM.DIGI.CUSTOMER,CUSTOMER-CREATION-SUCCESS'
    ),
    TRUE,
    'v1',
    NOW(),
    'SYSTEM',
    'SYSTEM'
);

INSERT INTO routing_key_config (
    template_identifiers,
    is_active,
    config_name,
    effective_from,
    created_by,
    updated_by
) VALUES (
    JSON_OBJECT(
        'alertType','LOAN_TERMINATION',
        'eventType','ArrangementEvents-LENDING-LOAN-TERMINATION-EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','EMAIL',
        'legalCompanyCode','RB',
        'application','LENDING.DIGI.LOANS,LENDING-LOAN-TERMINATION'
    ),
    TRUE,
    'v1',
    NOW(),
    'SYSTEM',
    'SYSTEM'
);
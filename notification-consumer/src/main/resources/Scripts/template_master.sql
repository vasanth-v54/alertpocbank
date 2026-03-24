CREATE TABLE template_master (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,

    message_type VARCHAR(50) NOT NULL COMMENT 'EMAIL / SMS / PUSH etc',

    template_identifiers_ref JSON NOT NULL COMMENT 'Reference to routing key config identifiers',

    template_parameters JSON NOT NULL COMMENT 'Template dynamic parameters',

    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Active flag',

    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,

    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) DEFAULT NULL
);

-- EMAIL

INSERT INTO template_master (
    message_type,
    template_identifiers_ref,
    template_parameters,
    is_active,
    created_by,
    updated_by
) VALUES (
    'EMAIL',
    JSON_OBJECT(
        'alertType','CUSTOMER_CREATED',
        'eventType','CustomerEvents-CustomerCreated_CU_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','EMAIL',
        'legalCompanyCode','RB',
        'application','CRM.DIGI.CUSTOMER,CUSTOMER-CREATION-SUCCESS'
    ),
    JSON_OBJECT(
        'from','noreply@eastwestbanker.com',
        'to','cnlagahit@eastwestbanker.com',
        'subject','Instapay Fund Transfer',
        'body','test 123',
        'template','EMAIL_VERIFICATION',
        'templateParams', JSON_ARRAY('customer_name', 'applicationcustomerid', 'timestamp'),
        'cc','johndoe@email.com',
        'bcc','johndoe@email.com'
    ),
    TRUE,
    'SYSTEM',
    'SYSTEM'
);

INSERT INTO template_master (
    message_type,
    template_identifiers_ref,
    template_parameters,
    is_active,
    created_by,
    updated_by
) VALUES (
    'EMAIL',
    JSON_OBJECT(
        'alertType','LOAN_DISBURSEMENT_SUCCESSFUL',
        'eventType','ArrangementEvents-LoanDisbursementSuccessful_AA_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','EMAIL',
        'legalCompanyCode','RB',
        'application','LENDING.DIGI.LOANS,LENDING-DISBURSEMENT-SUCCESS'
    ),
    JSON_OBJECT(
        'from','noreply@eastwestbanker.com',
        'to','cnlagahit@eastwestbanker.com',
        'subject','Instapay Fund Transfer',
        'body','test 123',
        'template','EMAIL_VERIFICATION',
        'templateParams', JSON_ARRAY('amount', 'DisbursementAccount', 'timestamp', 'ParentReference'),
        'cc','johndoe@email.com',
        'bcc','johndoe@email.com'
    ),
    TRUE,
    'SYSTEM',
    'SYSTEM'
);

INSERT INTO template_master (
    message_type,
    template_identifiers_ref,
    template_parameters,
    is_active,
    created_by,
    updated_by
) VALUES (
    'EMAIL',
    JSON_OBJECT(
        'alertType','LOAN_PAST_DUE_REMINDER',
        'eventType','ArrangementEvents-LoanPastDueReminder_AA_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','EMAIL',
        'legalCompanyCode','RB',
        'application','LENDING.DIGI.LOANS,LENDING-PASTDUE-REMINDER'
    ),
    JSON_OBJECT(
        'from','noreply@eastwestbanker.com',
        'to','cnlagahit@eastwestbanker.com',
        'subject','Instapay Fund Transfer',
        'body','test 123',
        'template','EMAIL_VERIFICATION',
        'templateParams', JSON_ARRAY('ImmediateParentReference', 'PastDueDays', 'OverdueAmount', 'grace_date', 'ParentReference', 'companyId'),
        'cc','johndoe@email.com',
        'bcc','johndoe@email.com'
    ),
    TRUE,
    'SYSTEM',
    'SYSTEM'
);

INSERT INTO template_master (
    message_type,
    template_identifiers_ref,
    template_parameters,
    is_active,
    created_by,
    updated_by
) VALUES (
    'EMAIL',
    JSON_OBJECT(
        'alertType','FUND_TRANSFER_SUCCESSFUL',
        'eventType','PaymentEvents-FundTransferSuccessful_TPH_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','EMAIL',
        'legalCompanyCode','RB',
        'application','TPH.DIGI.PAYMENTS,PAYMENTS-FUND-TRANSFER-SUCCESS'
    ),
    JSON_OBJECT(
        'from','noreply@eastwestbanker.com',
        'to','cnlagahit@eastwestbanker.com',
        'subject','Instapay Fund Transfer',
        'body','test 123',
        'template','EMAIL_VERIFICATION',
        'templateParams', JSON_ARRAY('amount', 'SenderAccount', 'ReceiverAccount', 'timestamp', 'ReferenceNumber'),
        'cc','johndoe@email.com',
        'bcc','johndoe@email.com'
    ),
    TRUE,
    'SYSTEM',
    'SYSTEM'
);


-- SMS
INSERT INTO template_master (
    message_type,
    template_identifiers_ref,
    template_parameters,
    is_active,
    created_by,
    updated_by
) VALUES (
    'SMS',
    JSON_OBJECT(
        'alertType','Account Inactivity(60 Days) - ATM Pensioner',
        'eventType','ewbIntegrationProject-AcInactivePreNoticeATMPensioner_AA_ACTIVITY_EVENT',
        'originatingSource','',
        'messageType','SMS',
        'legalCompanyCode','RB',
        'application','Accounts'
    ),
    JSON_OBJECT(
        'from','EASTWEST',
        'mobileNumber','',
        'message','123',
        'template','DEFAULT',
        'templateParams', JSON_ARRAY('param1','param2'),
        'notificationType','',
        'referenceId','',
        'callbackUrl','www.eastwestbanker.com'
    ),
    TRUE,
    'SYSTEM',
    'SYSTEM'
);

INSERT INTO template_master (
    message_type,
    template_identifiers_ref,
    template_parameters,
    is_active,
    created_by,
    updated_by
) VALUES (
    'SMS',
    JSON_OBJECT(
        'alertType','CUSTOMER_CREATED',
        'eventType','CustomerEvents-CustomerCreated_CU_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','EMAIL',
        'legalCompanyCode','RB',
        'application','CRM.DIGI.CUSTOMER,CUSTOMER-CREATION-SUCCESS'
    ),
    JSON_OBJECT(
        'from','EASTWEST',
        'mobileNumber','',
        'message','123',
        'template','DEFAULT',
        'templateParams', JSON_ARRAY('customer_name', 'applicationcustomerid', 'timestamp'),
        'notificationType','',
        'referenceId','',
        'callbackUrl','www.eastwestbanker.com'
    ),
    TRUE,
    'SYSTEM',
    'SYSTEM'
);


INSERT INTO template_master (
    message_type,
    template_identifiers_ref,
    template_parameters,
    is_active,
    created_by,
    updated_by
) VALUES (
    'SMS',
    JSON_OBJECT(
        'alertType','LOAN_DISBURSEMENT_SUCCESSFUL',
        'eventType','ArrangementEvents-LoanDisbursementSuccessful_AA_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','EMAIL',
        'legalCompanyCode','RB',
        'application','LENDING.DIGI.LOANS,LENDING-DISBURSEMENT-SUCCESS'
    ),
    JSON_OBJECT(
        'from','EASTWEST',
        'mobileNumber','',
        'message','123',
        'template','DEFAULT',
        'templateParams', JSON_ARRAY('amount', 'DisbursementAccount', 'timestamp', 'ParentReference'),
        'notificationType','',
        'referenceId','',
        'callbackUrl','www.eastwestbanker.com'
    ),
    TRUE,
    'SYSTEM',
    'SYSTEM'
);


INSERT INTO template_master (
    message_type,
    template_identifiers_ref,
    template_parameters,
    is_active,
    created_by,
    updated_by
) VALUES (
    'SMS',
    JSON_OBJECT(
        'alertType','LOAN_PAST_DUE_REMINDER',
        'eventType','ArrangementEvents-LoanPastDueReminder_AA_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','EMAIL',
        'legalCompanyCode','RB',
        'application','LENDING.DIGI.LOANS,LENDING-PASTDUE-REMINDER'
    ),
    JSON_OBJECT(
        'from','EASTWEST',
        'mobileNumber','',
        'message','123',
        'template','DEFAULT',
        'templateParams', JSON_ARRAY('ImmediateParentReference', 'PastDueDays', 'OverdueAmount', 'grace_date', 'ParentReference', 'companyId'),
        'notificationType','',
        'referenceId','',
        'callbackUrl','www.eastwestbanker.com'
    ),
    TRUE,
    'SYSTEM',
    'SYSTEM'
);


INSERT INTO template_master (
    message_type,
    template_identifiers_ref,
    template_parameters,
    is_active,
    created_by,
    updated_by
) VALUES (
    'SMS',
    JSON_OBJECT(
        'alertType','FUND_TRANSFER_SUCCESSFUL',
        'eventType','PaymentEvents-FundTransferSuccessful_TPH_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','EMAIL',
        'legalCompanyCode','RB',
        'application','TPH.DIGI.PAYMENTS,PAYMENTS-FUND-TRANSFER-SUCCESS'
    ),
    JSON_OBJECT(
        'from','EASTWEST',
        'mobileNumber','',
        'message','123',
        'template','DEFAULT',
        'templateParams', JSON_ARRAY('amount', 'SenderAccount', 'ReceiverAccount', 'timestamp', 'ReferenceNumber'),
        'notificationType','',
        'referenceId','',
        'callbackUrl','www.eastwestbanker.com'
    ),
    TRUE,
    'SYSTEM',
    'SYSTEM'
);

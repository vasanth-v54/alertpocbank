
CREATE TABLE template_master (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
	message_type VARCHAR(50) NOT NULL COMMENT 'EMAIL / SMS / PUSH etc',
    template_identifiers_ref JSON NOT NULL COMMENT 'Reference to routing key config identifiers',
	template_id varchar(255),
    template_body JSON NOT NULL COMMENT 'Template dynamic parameters',

    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'Active flag',

    create_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) NOT NULL,

    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(100) DEFAULT NULL
);
COMMIT;

-- Customer Created

INSERT INTO template_master (
    message_type,
    template_identifiers_ref,
    template_id,
    template_body,
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
	'TPL_EML_CUSTOMER_CREATED',
    JSON_OBJECT(
        'from','noreply@eastwestbanker.com',
        'to','cnlagahit@eastwestbanker.com',
        'subject','Instapay Fund Transfer',
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
    template_id,
    template_body,
    is_active,
    created_by,
    updated_by
) VALUES (
    'SMS',
    JSON_OBJECT(
        'alertType','CUSTOMER_CREATED',
        'eventType','CustomerEvents-CustomerCreated_CU_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','SMS',
        'legalCompanyCode','RB',
        'application','CRM.DIGI.CUSTOMER,CUSTOMER-CREATION-SUCCESS'
    ),'TPL_SMS_CUSTOMER_CREATED',
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



-- LOAN_DISBURSEMENT_SUCCESSFUL

INSERT INTO template_master (
    message_type,
    template_identifiers_ref,
    template_id,
    template_body,
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
    ),'TPL_EML_LOAN_DISBURSEMENT_SUCCESSFUL',
    JSON_OBJECT(
        'from','noreply@eastwestbanker.com',
        'to','cnlagahit@eastwestbanker.com',
        'subject','Instapay Fund Transfer',
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
    template_id,
    template_body,
    is_active,
    created_by,
    updated_by
) VALUES (
    'SMS',
    JSON_OBJECT(
        'alertType','LOAN_DISBURSEMENT_SUCCESSFUL',
        'eventType','ArrangementEvents-LoanDisbursementSuccessful_AA_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','SMS',
        'legalCompanyCode','RB',
        'application','LENDING.DIGI.LOANS,LENDING-DISBURSEMENT-SUCCESS'
    ),'TPL_SMS_LOAN_DISBURSEMENT_SUCCESSFUL',
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

--LOAN_PAST_DUE_REMINDER

INSERT INTO template_master (
    message_type,
    template_identifiers_ref,
    template_id,
    template_body,
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
    ),'TPL_EML_LOAN_PAST_DUE_REMINDER',
    JSON_OBJECT(
        'from','noreply@eastwestbanker.com',
        'to','cnlagahit@eastwestbanker.com',
        'subject','Instapay Fund Transfer',
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
    template_id,
    template_body,
    is_active,
    created_by,
    updated_by
) VALUES (
    'SMS',
    JSON_OBJECT(
        'alertType','LOAN_PAST_DUE_REMINDER',
        'eventType','ArrangementEvents-LoanPastDueReminder_AA_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','SMS',
        'legalCompanyCode','RB',
        'application','LENDING.DIGI.LOANS,LENDING-PASTDUE-REMINDER'
    ),'TPL_SMS_LOAN_PAST_DUE_REMINDER',
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

-- FUND_TRANSFER_SUCCESSFUL

INSERT INTO template_master (
    message_type,
    template_identifiers_ref,
    template_id,
    template_body,
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
    ),'TPL_EML_FUND_TRANSFER_SUCCESSFUL',
    JSON_OBJECT(
        'from','noreply@eastwestbanker.com',
        'to','cnlagahit@eastwestbanker.com',
        'subject','Instapay Fund Transfer',
        'template','EMAIL_VERIFICATION',
        'templateParams', JSON_ARRAY('amount', 'SenderAccount', 'ReceiverAccount', 'timestamp', 'ReferenceNumber'),
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
    template_id,
    template_body,
    is_active,
    created_by,
    updated_by
) VALUES (
    'SMS',
    JSON_OBJECT(
        'alertType','FUND_TRANSFER_SUCCESSFUL',
        'eventType','PaymentEvents-FundTransferSuccessful_TPH_ACTIVITY_EVENT',
        'originatingSource','https://temenos.com/microservice/cloudevents/POCBank',
        'messageType','SMS',
        'legalCompanyCode','RB',
        'application','TPH.DIGI.PAYMENTS,PAYMENTS-FUND-TRANSFER-SUCCESS'
    ),'TPL_SMS_FUND_TRANSFER_SUCCESSFUL',
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

INSERT INTO dxp.template_master (template_name, version, alert_config, headers, raw_content, indexed_content, param_mapping, contentHash, exceptionReason, isDuplicateallowed, is_active, created_by, modified_by, created_date, modified_date, message_type, created_at, updated_at, updated_by) VALUES('Accounts_accountdeactivated', '1.0.0', '{"domain": "Accounts", "alertId": "ALERT_001", "alertName": "Notice of Zero Balance for 60 days", "alertType": "NOTICE", "eventType": "ZERO_BALANCE_60D", "leadCompanyCode": null, "originatingSource": null}', '{"headers_sms": {"to": "{{CustomerMobile}}"}}', '{
  "rawcontent_sms": "Hi {{CustomerName}}, zero balance alert on account ending {{CustomerAccountNo_Last4digits}}."
}', '{"indexed_content_sms": "Hi {0}, zero balance alert on account ending {1}."}', '{"param_mapping_sms": [{"seq": 0, "parameter": "CustomerName", "mappingType": "DIRECT", "augExpression": null}, {"seq": 1, "parameter": "CustomerAccountNo_Last4digits", "mappingType": "DIRECT", "augExpression": null}]}', NULL, NULL, 0, '1', 'SYSTEM', 'SYSTEM', '2026-04-06 09:37:54', '2026-04-06 09:37:54', 'SMS', NULL, NULL, NULL);




INSERT INTO dxp.template_master (template_name, version, alert_config, headers, raw_content, indexed_content, param_mapping, contentHash, exceptionReason, isDuplicateallowed, is_active, created_by, modified_by, created_date, modified_date, message_type, created_at, updated_at, updated_by) VALUES('Accounts_accountdeactivated', '1.0.0', '{"domain": "Accounts", "alertId": "ALERT_001", "alertName": "Notice of Zero Balance for 60 days", "alertType": "NOTICE", "eventType": "ZERO_BALANCE_60D", "leadCompanyCode": null, "originatingSource": null}', '{"headers_email": {"cc": "ops@bank.com", "to": "{{CustomerEmail}}", "bcc": "", "from": "noreply@ebw.com", "subject": "{{AlertSubject}}"}}', '{
  "rawcontent_email": "Dear {{CustomerName}}, your account {{CustomerAccountNo_Last4digits}} has a low balance of {{TransactionAmount}}."
}', '{"indexed_content_email": "Dear {0}, your account {1} has a low balance of {2}."}', '{"param_mapping_email": [{"seq": 0, "parameter": "CustomerName", "mappingType": "DIRECT", "augExpression": null}, {"seq": 1, "parameter": "CustomerAccountNo_Last4digits", "mappingType": "DIRECT", "augExpression": null}, {"seq": 2, "parameter": "TransactionAmount", "mappingType": "DIRECT", "augExpression": null}]}', NULL, NULL, 0, '1', 'SYSTEM', 'SYSTEM', '2026-04-06 09:37:31', '2026-04-06 09:37:31', 'EMAIL', NULL,  NULL,NULL);



-- data insertion for both dxp_email_template and dxp_sms_template table


INSERT INTO dxp.dxp_email_template
(
    TEMPLATE_NAME,
    template_body,
    IS_ACTIVE,
    DATE_CREATED,
    DATE_UPDATED
)
VALUES
    (
        'Accounts_accountdeactivated',
        JSON_OBJECT(
                'indexed_content_email',
                'Dear {0}, your account {1} has a low balance of {2}.'
        ),
        1,
        NOW(),
        NOW()
    );

INSERT INTO dxp.dxp_sms_template
(
    TEMPLATE_NAME,
    template_body,
    IS_ACTIVE,
    DATE_CREATED,
    DATE_UPDATED
)
VALUES
    (
        'Accounts_accountdeactivated',
        JSON_OBJECT(
                'indexed_content_sms',
                'Hi {0}, your account {1} has a low balance of {2}.'
        ),
        1,
        NOW(),
        NOW()
    );
desc dxp.dxp_sms_template

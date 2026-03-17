package com.poc.alerts.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PayloadValidator {

    public static String validatePayload(String payload) {

        try {

            ObjectMapper mapper = new ObjectMapper();

            JsonNode root = mapper.readTree(payload);

            JsonNode custom =
                    root.path("payload")
                            .path("customFieldDetails");

            String[] fields = {

                    "customer_id",
                    "customer_name",
                    "timestamp",
                    "amount",
                    "loan_account",
                    "txn_ref",
                    "days_overdue",
                    "min_amount_due",
                    "grace_date",
                    "loan_ref",
                    "beneficiary_name",
                    "beneficiary_id"
            };

            for (String f : fields) {

                JsonNode node = custom.get(f);

                if (node == null ||
                        node.isNull() ||
                        node.asText().trim().isEmpty()) {

                    return f;
                }
            }

        } catch (Exception e) {

            return "INVALID_JSON";
        }

        return null;
    }
}
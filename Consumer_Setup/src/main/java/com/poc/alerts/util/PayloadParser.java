package com.poc.alerts.util;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.alerts.entity.ConsumerEntryAudit;

public class PayloadParser {

    private static final Logger templateLog =
            LoggerFactory.getLogger("TEMPLATE_PROCESS_LOGGER");
    
    public static Map<String,String> flatten(String payload){

        Map<String,String> result = new HashMap<>();

        try{

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(payload);

            flattenNode("", root, result);

        }catch(Exception e){

            throw new RuntimeException("Payload parsing error", e);
        }

        return result;
    }

    @SuppressWarnings("deprecation")
	private static void flattenNode(String prefix,
                                    JsonNode node,
                                    Map<String,String> result){

        if(node.isObject()){

            Iterator<Map.Entry<String, JsonNode>> fields =
                    node.fields();

            while(fields.hasNext()){

                Map.Entry<String, JsonNode> entry = fields.next();

                String key = prefix.isEmpty()
                        ? entry.getKey()
                        : prefix + "." + entry.getKey();

                flattenNode(key, entry.getValue(), result);
            }
        }

        else if(node.isArray()){

            for(int i=0;i<node.size();i++){

                flattenNode(prefix+"["+i+"]",
                        node.get(i),
                        result);
            }
        }

        else{

            result.put(prefix,node.asText());
        }
    }

    public static String extractType(String json) {

        try {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            return root.path("payload")
                       .path("customFieldDetails")
                       .path("MessageType")
                       .asText();

        } catch (Exception e) {
            return null;
        }
    }

    public static String extractEventType(String json) {

        try {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            return root.path("eventType").asText();

        } catch (Exception e) {
            return null;
        }
    }

    public static String extractAlertType(String json) {

        try {

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            return root.path("payload")
                       .path("customFieldDetails")
                       .path("alertType")
                       .asText();

        } catch (Exception e) {
            return null;
        }
    }

    public static Map<String,String> extractFieldsBasedOnAlertType(
            String payload,
            String alertType) throws Exception {

        templateLog.info("------------------------------------------------");
        templateLog.info("PAYLOAD DATA EXTRACTION PROCESS STARTED");
        templateLog.info("PAYLOAD : {}", payload);
        templateLog.info("AlertType : {}", alertType);

        Map<String,String> values = new HashMap<>();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(payload);

        JsonNode custom =
                root.path("payload").path("customFieldDetails");

        JsonNode customCommon =
                root.path("payload")
                    .path("eventCommon")
                    .path("customCommon");

        JsonNode eventCommon =
                root.path("payload")
                    .path("eventCommon");

        String customerId = custom.path("customer_id").asText();

        String last4 = "";

        if(customerId != null && customerId.length() >= 4){
            last4 = customerId.substring(customerId.length() - 4);
        }

        String eventId = root.path("eventId").asText();

        switch(alertType){

            case "CUSTOMER_CREATED":

                values.put("customer_name",
                        custom.path("customer_name").asText());

                values.put("customer_id_last4", last4);

                values.put("timestamp",
                        custom.path("timestamp").asText());

                values.put("event_id", eventId);

                break;

            case "ACCOUNT_CREATED":

                values.put("customer_name",
                        custom.path("customer_name").asText());

                values.put("customer_id_last4", last4);

                values.put("timestamp",
                        eventCommon.path("creationTime").asText());

                values.put("event_id", eventId);

                break;

            case "LOAN_DISBURSEMENT_SUCCESSFUL":

                values.put("customer_name",
                        custom.path("customer_name").asText());

                String loanAccount =
                        custom.path("loan_account").asText();

                String loanLast4 = "";

                if(loanAccount != null && loanAccount.length() >= 4){
                    loanLast4 =
                            loanAccount.substring(
                                    loanAccount.length() - 4);
                }

                values.put("loan_account_last4", loanLast4);

                values.put("amount",
                        custom.path("amount").asText());

                values.put("txn_ref",
                        custom.path("txn_ref").asText());

                values.put("timestamp",
                        custom.path("timestamp").asText());

                values.put("event_id", eventId);

                break;

            case "LOAN_PAST_DUE_REMINDER":

                values.put("customer_name",
                        custom.path("customer_name").asText());

                values.put("days_overdue",
                        custom.path("days_overdue").asText());

                loanAccount =
                        custom.path("loan_account").asText();

                loanLast4 = "";

                if(loanAccount != null && loanAccount.length() >= 4){
                    loanLast4 =
                            loanAccount.substring(
                                    loanAccount.length() - 4);
                }

                values.put("loan_account_last4", loanLast4);

                values.put("min_amount_due",
                        custom.path("min_amount_due").asText());

                values.put("grace_date",
                        custom.path("grace_date").asText());

                values.put("loan_ref",
                        custom.path("loan_ref").asText());

                values.put("event_id", eventId);

                break;

            case "FUND_TRANSFER_SUCCESSFUL":

                values.put("customer_name",
                        custom.path("customer_name").asText());

                values.put("amount",
                        custom.path("amount").asText());

                String senderAccount =
                        customCommon.path("SenderAccount").asText();

                String receiverAccount =
                        customCommon.path("ReceiverAccount").asText();

                String receiverName =
                        customCommon.path("ReceiverName").asText();

                String referenceNumber =
                        customCommon.path("ReferenceNumber").asText();

                String accountLast4 = "";

                if(senderAccount != null && senderAccount.length() >= 4){
                    accountLast4 =
                            senderAccount.substring(
                                    senderAccount.length() - 4);
                }

                String beneficiaryMasked = "";

                if(receiverAccount != null && receiverAccount.length() >= 4){
                    beneficiaryMasked =
                            "****" + receiverAccount.substring(
                                    receiverAccount.length() - 4);
                }

                values.put("account_last4", accountLast4);
                values.put("beneficiary_masked", beneficiaryMasked);
                values.put("beneficiary_name", receiverName);
                values.put("txn_ref", referenceNumber);
                values.put("timestamp",
                        custom.path("timestamp").asText());
                values.put("event_id", eventId);

                break;

            default:
                break;
        }

        return values;
    }

    public static ConsumerEntryAudit parse(String topic,
                                           String json){

        ConsumerEntryAudit audit =
                new ConsumerEntryAudit();

        try{

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            audit.setTopic(topic);
            audit.setReceivedTime(LocalDateTime.now());

            audit.setBusinessKey(root.path("businessKey").asText());
            audit.setSourceUri(root.path("sourceUri").asText());
            audit.setStatus(root.path("status").asText());
            audit.setEventType(root.path("eventType").asText());
            audit.setEventId(root.path("eventId").asText());

            JsonNode payload = root.path("payload");

            audit.setApplication(
                    payload.path("eventCommon")
                           .path("application")
                           .asText());

            JsonNode custom =
                    payload.path("customFieldDetails");

            audit.setMessageType(
                    custom.path("MessageType").asText());

            audit.setAlertType(
                    custom.path("alertType").asText());

            audit.setReleaseVersion(
                    custom.path("Release").asText());

            audit.setFullPayload(json);

        }
        catch(Exception e){
            e.printStackTrace();
        }

        return audit;
    }

    public static String extractEventId(String json){

        try{

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);

            return root.path("eventId").asText();

        }catch(Exception e){
            return null;
        }
    }
}
package com.notification.consumer.dlt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class DltServiceImpl implements DltService {

    private final ObjectMapper mapper = new ObjectMapper();

    private static final String FILE_NAME = "DLTlog.json";

    @Override
    public void logDlt(String payload,
                       Map<String, String> headers,
                       String errorMessage) {

        try {

            ObjectNode root = mapper.createObjectNode();

            root.put("severity", "ERROR");

            ObjectNode dataNode = mapper.createObjectNode();
            dataNode.put("Runtime", "Java");

            ObjectNode messageNode = mapper.createObjectNode();

            messageNode.put("headers", headers.toString());
            messageNode.put("eventId",
                    headers.getOrDefault("event-id", "UNKNOWN_EVENT_ID"));
            messageNode.put("payload", payload);
            messageNode.put("errorMessage", errorMessage);

            dataNode.set("Message", messageNode);
            dataNode.put("Method", "logDlt");

            root.set("data", dataNode);

            root.put("service", "ConsumerService");
            root.put("environment", "DEVELOPMENT");
            root.put("sessionId", UUID.randomUUID().toString());
            root.put("statusCode", "400");
            root.put("application", "alertPoc");
            root.put("correlationId", UUID.randomUUID().toString());
            root.put("browserType", "Edge");
            root.put("eventTimeUTC", Instant.now().toString());

            writeToFile(root);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private synchronized void writeToFile(ObjectNode newLog) {

        try {

            File file = new File(FILE_NAME);

            com.fasterxml.jackson.databind.node.ArrayNode arrayNode;

            // 🔥 Case 1: File doesn't exist OR empty
            if (!file.exists() || file.length() == 0) {

                arrayNode = mapper.createArrayNode();

            } else {

                try {
                    arrayNode = (com.fasterxml.jackson.databind.node.ArrayNode) mapper.readTree(file);

                    // 🔥 If file content is not array → reset
                    if (arrayNode == null || !arrayNode.isArray()) {
                        arrayNode = mapper.createArrayNode();
                    }

                } catch (Exception ex) {
                    // 🔥 If corrupted → reset file
                    arrayNode = mapper.createArrayNode();
                }
            }

            // 🔥 Add new log
            arrayNode.add(newLog);

            // 🔥 Write back safely
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, arrayNode);

        } catch (Exception e) {
            System.err.println("DLT Write Failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
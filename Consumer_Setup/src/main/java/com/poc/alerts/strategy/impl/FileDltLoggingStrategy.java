package com.poc.alerts.strategy.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.poc.alerts.entity.DltLog;
import com.poc.alerts.strategy.DltLoggingStrategy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileDltLoggingStrategy implements DltLoggingStrategy {

    private static final String FILE_PATH = "logs/dlt-log.json";

    @Override
    public synchronized void log(DltLog log) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);

            File file = new File(FILE_PATH);
            List<DltLog> logs;

            // If file exists and is not empty, read its content
            if (file.exists() && file.length() > 0) {
                try {
                    logs = mapper.readValue(file, new TypeReference<List<DltLog>>() {});
                } catch (IOException e) {
                    // If the file is corrupted or not a valid JSON array, start fresh
                    logs = new ArrayList<>();
                }
            } else {
                logs = new ArrayList<>();
            }

            // Add the new log entry
            logs.add(log);

            // Write the updated list back to the file
            try (FileWriter writer = new FileWriter(FILE_PATH, false)) { // Overwrite the file
                mapper.writeValue(writer, logs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.poc.alerts.strategy.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.alerts.entity.DltLog;
import com.poc.alerts.strategy.DltLoggingStrategy;

import java.io.FileWriter;

public class FileDltLoggingStrategy implements DltLoggingStrategy {

    private static final String FILE_PATH = "logs/dlt-log.json";

    @Override
    public void log(DltLog log) {

        try {

            ObjectMapper mapper = new ObjectMapper();

            String json = mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(log);

            FileWriter writer = new FileWriter(FILE_PATH, true);

            writer.write(json);
            writer.write("\n");

            writer.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}
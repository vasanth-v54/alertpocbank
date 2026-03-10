package com.poc.alerts.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.poc.alerts.util.PayloadParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PayloadVariableExtractor {

    private static final Logger log =
            LoggerFactory.getLogger(PayloadVariableExtractor.class);

    public Map<String,String> extractVariables(
            String payload,
            String templateParams){

        log.info("Flattening payload JSON");

        Map<String,String> flattened =
                PayloadParser.flatten(payload);

        Map<String,String> values = new HashMap<>();

        try {

            ObjectMapper mapper = new ObjectMapper();

            List<String> variables =
                    mapper.readValue(
                            templateParams,
                            new TypeReference<List<String>>() {}
                    );

            for(String variable : variables){

                String value = findValue(flattened,variable);

                log.info("Variable {} resolved to {}",variable,value);

                values.put(variable,value);
            }

        } catch (Exception e) {

            log.error("Error parsing template params JSON", e);
        }

        return values;
    }

    private String findValue(Map<String,String> flattened,
                             String variable){

        for(Map.Entry<String,String> entry : flattened.entrySet()){

            if(entry.getKey()
                    .toLowerCase()
                    .contains(variable.toLowerCase())){

                return entry.getValue();
            }
        }

        return "";
    }
}
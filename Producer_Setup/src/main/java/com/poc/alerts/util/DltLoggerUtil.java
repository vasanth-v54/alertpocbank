package com.poc.alerts.util;

import com.poc.alerts.entity.DltLog;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DltLoggerUtil {

    public static DltLog build(String service,
                               String statusCode,
                               String headers,
                               String eventId,
                               String errorMessage,
                               String payload) {

        DltLog log = new DltLog();

        log.setSessionId(UUID.randomUUID().toString());
        log.setCorrelationId(UUID.randomUUID().toString());
        log.setEnvironment("DEVELOPMENT");
        log.setStatusCode(statusCode);
        log.setSeverity("ERROR");
        log.setApplication("alertPoc");
        log.setService(service);
        log.setEventTimeUTC(Instant.now().toString());
        log.setBrowserType("Edge");

        Map<String, Object> message = new HashMap<>();

        message.put("headers", headers);
        message.put("eventId", eventId);
        message.put("errorMessage", errorMessage);
        message.put("payload", payload);

        Map<String, Object> data = new HashMap<>();

        data.put("Runtime", "Java");
        data.put("Message", message);
        data.put("Method", "logDlt");

        log.setData(data);

        return log;
    }
}
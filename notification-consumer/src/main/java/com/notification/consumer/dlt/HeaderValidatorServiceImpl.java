package com.notification.consumer.dlt;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class HeaderValidatorServiceImpl implements HeaderValidatorService {

    private final DltService dltService;

    public HeaderValidatorServiceImpl(DltService dltService) {
        this.dltService = dltService;
    }

    @Override
    public void validateHeaders(Map<String, String> headers, String payload) {

        validate(headers.get("event-type"), "event-type", headers, payload);
        validate(headers.get("event-id"), "event-id", headers, payload);
       // validate(headers.get("alert-type"), "alert-type", headers, payload);
        validate(headers.get("MessageType"), "MessageType", headers, payload);
        validate(headers.get("status"), "status", headers, payload);
       // validate(headers.get("originatingsource"), "originatingsource", headers, payload);
    }

    private void validate(String value,
                          String field,
                          Map<String, String> headers,
                          String payload) {

        if (isInvalid(value)) {

            String errorMessage = "Header validation failed: Missing or Empty " + field;

            dltService.logDlt(payload, headers, errorMessage);

            throw new RuntimeException(errorMessage);
        }
    }

    private boolean isInvalid(String value) {
        return value == null
                || value.trim().isEmpty()
                || value.trim().equalsIgnoreCase("null");
    }
}
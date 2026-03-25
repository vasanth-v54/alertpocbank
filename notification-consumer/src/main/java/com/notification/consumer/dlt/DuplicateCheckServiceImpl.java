package com.notification.consumer.dlt;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DuplicateCheckServiceImpl implements DuplicateCheckService {

    // Thread-safe set (important for Kafka concurrency)
    private final Set<String> processedEventIds = ConcurrentHashMap.newKeySet();

    private final DltService dltService;

    public DuplicateCheckServiceImpl(DltService dltService) {
        this.dltService = dltService;
    }

    @Override
    public void checkDuplicate(String eventId,
                               String payload,
                               java.util.Map<String, String> headers) {

        if (eventId == null || eventId.trim().isEmpty()
                || eventId.equalsIgnoreCase("null")) {
            return; // already handled in header validation
        }

        // 🔥 Duplicate check
        if (!processedEventIds.add(eventId)) {

            String errorMessage = "Duplicate message received for eventId: " + eventId;

            dltService.logDlt(payload, headers, errorMessage);

            throw new RuntimeException(errorMessage);
        }
    }
}
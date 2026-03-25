package com.notification.consumer.dlt;

import java.util.Map;

public interface DltService {

    void logDlt(String payload,
                Map<String, String> headers,
                String errorMessage);

}
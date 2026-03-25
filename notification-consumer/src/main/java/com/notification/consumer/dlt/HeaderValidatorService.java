package com.notification.consumer.dlt;

import java.util.Map;

public interface HeaderValidatorService {

    void validateHeaders(Map<String, String> headers, String payload);

}
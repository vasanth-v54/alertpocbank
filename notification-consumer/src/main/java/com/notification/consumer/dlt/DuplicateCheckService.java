package com.notification.consumer.dlt;

public interface DuplicateCheckService {

    void checkDuplicate(String eventId, String payload, java.util.Map<String, String> headers);

}
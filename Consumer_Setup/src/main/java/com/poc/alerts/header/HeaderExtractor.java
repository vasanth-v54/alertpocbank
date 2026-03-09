package com.poc.alerts.header;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

public class HeaderExtractor {

    public static String extractHeader(ConsumerRecord<?, ?> record, String headerKey) {

        Header header = record.headers().lastHeader(headerKey);

        if (header == null) {
            throw new MissingHeaderException("Missing header: " + headerKey);
        }

        return new String(header.value());
    }
}

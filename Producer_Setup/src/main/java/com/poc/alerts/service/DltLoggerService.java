package com.poc.alerts.service;

import com.poc.alerts.entity.DltLog;
import com.poc.alerts.strategy.DltLoggingStrategy;

public class DltLoggerService {

    private final DltLoggingStrategy strategy;

    public DltLoggerService(DltLoggingStrategy strategy) {
        this.strategy = strategy;
    }

    public void log(DltLog log) {

        strategy.log(log);
    }
}
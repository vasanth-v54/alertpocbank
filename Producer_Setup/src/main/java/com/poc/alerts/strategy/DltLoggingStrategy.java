package com.poc.alerts.strategy;

import com.poc.alerts.entity.DltLog;

public interface DltLoggingStrategy {

    void log(DltLog log);
}
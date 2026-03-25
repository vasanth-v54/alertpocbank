package com.notification.consumer.logger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class VerticalLoggerImpl implements VerticalLogger {

    private static final Logger log =
            LoggerFactory.getLogger("VERTICAL_LOGGER");

    private final Map<String, Instant> stageTimers =
            new ConcurrentHashMap<>();

    private static final String THICK =
            "================================================================================";
    private static final String THIN  =
            "--------------------------------------------------------------------------------";

    // ── Stage open ────────────────────────────────────────────────────────────

    @Override
    public void stageStart(int stageNum, String stageName, String eventId) {
        stageTimers.put(timerKey(eventId, stageNum), Instant.now());

        log.info(THICK);
        log.info(">>> STAGE {}  STARTED    :  {}", stageNum, stageName);
        log.info("    EVENT_ID             :  {}", eventId != null ? eventId : "N/A");
        log.info(THIN);
    }

    // ── Stage close success ───────────────────────────────────────────────────

    @Override
    public void stageEnd(int stageNum, String stageName,
                         String status, String eventId) {
        String elapsed = elapsed(eventId, stageNum);

        log.info(THIN);
        log.info("<<< STAGE {}  COMPLETED  :  {}  |  STATUS : {}  |  TIME : {}",
                stageNum, stageName, status, elapsed);
        log.info(THICK);
        log.info("");

        stageTimers.remove(timerKey(eventId, stageNum));
    }

    // ── Stage close failure ───────────────────────────────────────────────────

    @Override
    public void stageError(int stageNum, String stageName,
                           String errorMsg, Throwable ex, String eventId) {
        String elapsed = elapsed(eventId, stageNum);

        log.info(THIN);
        log.error("!!! STAGE {}  FAILED     :  {}  |  ERROR : {}  |  TIME : {}",
                stageNum, stageName, errorMsg, elapsed);
        if (ex != null) {
            log.error("    EXCEPTION CLASS      :  {}", ex.getClass().getSimpleName());
            log.error("    EXCEPTION MSG        :  {}", ex.getMessage());
        }
        log.info(THICK);
        log.info("");

        stageTimers.remove(timerKey(eventId, stageNum));
    }

    // ── Field ─────────────────────────────────────────────────────────────────

    @Override
    public void field(String key, String value) {
        log.info("    {}{} :  {}",
                key,
                " ".repeat(Math.max(0, 22 - key.length())),
                value != null ? value : "NULL");
    }

    // ── Section ───────────────────────────────────────────────────────────────

    @Override
    public void section(String title) {
        log.info("  [ {} ]", title);
    }

    // ── Payload block ─────────────────────────────────────────────────────────

    @Override
    public void payload(String content) {
        log.info("  [ PAYLOAD ]");
        log.info("    {}", content);
    }

    // ── Multiple fields at once ───────────────────────────────────────────────

    @Override
    public void fields(Map<String, String> keyValuePairs) {
        if (keyValuePairs == null) return;
        keyValuePairs.forEach(this::field);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String timerKey(String eventId, int stageNum) {
        return (eventId != null ? eventId : "UNKNOWN") + "_STAGE_" + stageNum;
    }

    private String elapsed(String eventId, int stageNum) {
        Instant start = stageTimers.get(timerKey(eventId, stageNum));
        if (start == null) return "N/A";
        long ms = Duration.between(start, Instant.now()).toMillis();
        return ms < 1000 ? ms + " ms" : String.format("%.2f sec", ms / 1000.0);
    }
}
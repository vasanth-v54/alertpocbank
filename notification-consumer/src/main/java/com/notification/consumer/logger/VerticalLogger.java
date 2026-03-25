package com.notification.consumer.logger;

import java.util.Map;

/**
 * PipelineLogger — Loosely coupled logging interface for the
 * notification pipeline. All 6 stages call this.
 */


public interface VerticalLogger {

    // ── Stage lifecycle ───────────────────────────────────────────────────────

    /** Open a stage block. Starts internal timer for this stage. */
    void stageStart(int stageNum, String stageName, String eventId);

    /** Close a stage block with SUCCESS or SKIPPED. Logs time taken. */
    void stageEnd(int stageNum, String stageName, String status, String eventId);

    /** Close a stage block with FAILED. Logs error + time taken. */
    void stageError(int stageNum, String stageName,
                    String errorMsg, Throwable ex, String eventId);

    // ── Content inside a stage ────────────────────────────────────────────────

    /** Write a key-value field line. */
    void field(String key, String value);

    /** Write a sub-section header inside a stage. */
    void section(String title);

    /** Write raw payload JSON or any multi-line content. */
    void payload(String content);

    /** Write a map of fields — useful for headers, templateData etc. */
    void fields(Map<String, String> keyValuePairs);
}
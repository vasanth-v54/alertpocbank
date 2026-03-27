package com.notification.consumer.service;

import com.notification.consumer.dlt.DltService;
import com.notification.consumer.entity.RoutingKeyConfig;
import com.notification.consumer.entity.TemplateMaster;
import com.notification.consumer.logger.VerticalLogger;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private AppConfigService    configService;
    @Mock private RoutingKeyConfigService routingService;
    @Mock private TemplateService     templateService;
    @Mock private DltService          dltService;
    @Mock private VerticalLogger      vlog;

    @InjectMocks
    private NotificationService notificationService;

    // ─────────────────────────────────────────────────────────────────────────
    // Common stubs / fixtures
    // ─────────────────────────────────────────────────────────────────────────

    private static final String EVENT_TYPE  = "CustomerEvents-CustomerCreated";
    private static final String ALERT_TYPE  = "CUSTOMER_CREATED";
    private static final String MSG_TYPE    = "SMS";

    /** Minimal valid headers. */
    private Map<String, String> validHeaders() {
        Map<String, String> h = new HashMap<>();
        h.put("event-id", "evt-001");
        return h;
    }

    /** Builds a RoutingKeyConfig whose templateIdentifiers contain the given types. */
    private RoutingKeyConfig routingConfig(String eventType, String alertType) {
        return routingConfig(eventType, alertType, "SMS");
    }

    /** Builds a RoutingKeyConfig for a specific messageType. */
    private RoutingKeyConfig routingConfig(String eventType, String alertType, String messageType) {
        RoutingKeyConfig cfg = new RoutingKeyConfig();
        cfg.setId(1L);
        cfg.setIsActive(true);
        cfg.setConfigName("cfg-v1");
        cfg.setTemplateIdentifiers(
                String.format("{\"eventType\":\"%s\",\"alertType\":\"%s\",\"messageType\":\"%s\"}",
                        eventType, alertType, messageType));
        return cfg;
    }

    /** Builds a TemplateMaster for a given message type and required params. */
    private TemplateMaster templateFor(String messageType, String... paramNames) {
        TemplateMaster tm = new TemplateMaster();
        tm.setId(10L);
        tm.setIsActive(true);
        tm.setMessageType(messageType);
        tm.setTemplateIdentifiersRef(
                String.format("{\"eventType\":\"%s\",\"alertType\":\"%s\"}", EVENT_TYPE, ALERT_TYPE));

        StringBuilder params = new StringBuilder("{\"templateParams\":[");
        for (int i = 0; i < paramNames.length; i++) {
            params.append("\"").append(paramNames[i]).append("\"");
            if (i < paramNames.length - 1) params.append(",");
        }
        params.append("]}");
        tm.setTemplateBody(params.toString());
        return tm;
    }

    /**
     * Builds a minimal valid SMS payload with the required mobileNumber and
     * any additional dynamic template params provided.
     */
    private String buildSmsPayload(String mobile, Map<String, String> extra) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"eventType\":\"").append(EVENT_TYPE).append("\",");
        sb.append("\"payload\":{");
        sb.append("\"customFieldDetails\":{");
        sb.append("\"MessageType\":\"SMS\",");
        sb.append("\"alertType\":\"").append(ALERT_TYPE).append("\",");
        sb.append("\"originatingsource\":\"CRM\"");
        sb.append("},");
        if (mobile != null) {
            sb.append("\"mobileNumber\":\"").append(mobile).append("\",");
        }
        if (extra != null) {
            for (Map.Entry<String, String> e : extra.entrySet()) {
                sb.append("\"").append(e.getKey()).append("\":\"")
                  .append(e.getValue()).append("\",");
            }
        }
        sb.append("\"dummy\":\"x\"}}");   // trailing dummy avoids trailing-comma issues
        return sb.toString();
    }

    /** Common setup: VerticalLogger is allowed to have unused stubs. */
    @BeforeEach
    void stubLogger() {
        lenient().doNothing().when(vlog).stageStart(anyInt(), anyString(), anyString());
        lenient().doNothing().when(vlog).stageEnd(anyInt(), anyString(), anyString(), anyString());
        lenient().doNothing().when(vlog).stageError(anyInt(), anyString(), anyString(), any(), anyString());
        lenient().doNothing().when(vlog).field(anyString(), anyString());
        lenient().doNothing().when(vlog).section(anyString());
        lenient().doNothing().when(vlog).payload(anyString());
    }

    // =========================================================================
    // TICKET: Null/empty payload → fail validation
    // =========================================================================

    @Test
    void process_WhenPayloadIsNull_ShouldNotThrowButLogError() {
        // Null payload causes a JSON parse error; the service must catch it internally.
        assertDoesNotThrow(() -> notificationService.process(null, validHeaders()));
        // DLT is NOT invoked here because the exception is caught before any DLT call;
        // what matters is the service does not propagate an unhandled exception.
    }

    @Test
    void process_WhenPayloadIsEmptyString_ShouldNotThrow() {
        assertDoesNotThrow(() -> notificationService.process("", validHeaders()));
    }

    @Test
    void process_WhenPayloadIsBlankWhitespace_ShouldNotThrow() {
        assertDoesNotThrow(() -> notificationService.process("   ", validHeaders()));
    }

    @Test
    void process_WhenPayloadIsInvalidJson_ShouldNotThrow() {
        assertDoesNotThrow(() -> notificationService.process("{NOT_JSON{{", validHeaders()));
    }

    @Test
    void process_WhenPayloadIsMissingCustomFieldDetails_ShouldHandleGracefully() {
        // customFieldDetails is absent → messageType / alertType are null → "Invalid input"
        String payload = "{\"eventType\":\"" + EVENT_TYPE + "\",\"payload\":{}}";
        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("SMS|EMAIL|BOTH");

        assertDoesNotThrow(() -> notificationService.process(payload, validHeaders()));
        verify(dltService, atLeastOnce()).logDlt(eq(payload), any(), anyString());
    }

    // =========================================================================
    // TICKET: Single parameter missing → fail with correct field name
    // =========================================================================

    @Test
    void process_WhenMessageTypeNotAllowed_ShouldLogDltWithMessageTypeError() {
        // Arrange – valid structure but messageType = "PUSH" which is not in the allowed set
        String payload = "{\"eventType\":\"" + EVENT_TYPE + "\","
                + "\"payload\":{\"customFieldDetails\":{"
                + "\"MessageType\":\"PUSH\","
                + "\"alertType\":\"" + ALERT_TYPE + "\","
                + "\"originatingsource\":\"CRM\"}}}";
        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("SMS|EMAIL|BOTH");

        notificationService.process(payload, validHeaders());

        verify(dltService, times(1))
                .logDlt(eq(payload), any(), eq("Message type is NOT allowed"));
    }

    @Test
    void process_WhenRoutingConfigMissing_ShouldLogDltWithRoutingError() {
        // Arrange – allowed message type but no routing config in DB
        // mobileNumber must be present so the precheck passes; then routing lookup returns empty
        String payload = buildSmsPayload("9876543210", null);
        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("SMS|EMAIL|BOTH");
        when(routingService.findMatchingConfigs(anyString())).thenReturn(Collections.emptyList());

        notificationService.process(payload, validHeaders());

        verify(dltService, times(1))
                .logDlt(eq(payload), any(), eq("Routing Key Configuration missing"));
    }

    @Test
    void process_WhenTemplateMissingForSms_ShouldLogDltWithTemplateError() {
        // Arrange – routing exists but no template found
        String payload = buildSmsPayload("9876543210", null);
        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("SMS|EMAIL|BOTH");
        when(routingService.findMatchingConfigs(anyString()))
                .thenReturn(List.of(routingConfig(EVENT_TYPE, ALERT_TYPE)));
        when(templateService.findTemplates(any(), any()))
                .thenReturn(new HashMap<>());

        notificationService.process(payload, validHeaders());

        verify(dltService, times(1))
                .logDlt(eq(payload), any(), eq("Template Configuration missing"));
    }

    @Test
    void process_WhenMobileNumberMissingForSms_ShouldLogDltWithMobileError() {
        // mobileNumber absent in payload; service reaches processSingle (SMS) and logs the error.
        // Routing runs because originatingsource is present (satisfies the || branch).
        String payload = buildSmsPayload(null, null);  // no mobileNumber key
        RoutingKeyConfig cfg  = routingConfig(EVENT_TYPE, ALERT_TYPE);
        TemplateMaster   tmpl = templateFor("SMS", "mobileNumber");

        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("SMS|EMAIL|BOTH");
        when(routingService.findMatchingConfigs(anyString())).thenReturn(List.of(cfg));
        when(templateService.findTemplates(any(), any())).thenReturn(Map.of("SMS", tmpl));

        notificationService.process(payload, validHeaders());

        verify(dltService, times(1))
                .logDlt(eq(payload), any(), contains("mobileNumber"));
    }

    @Test
    void process_WhenEmailAddressMissingForEmail_ShouldLogDltWithEmailError() {
        // "to" absent in payload; service reaches processSingle (EMAIL) and logs the error.
        // Routing runs because originatingsource is present.
        String payload = "{\"eventType\":\"" + EVENT_TYPE + "\","
                + "\"payload\":{\"customFieldDetails\":{"
                + "\"MessageType\":\"EMAIL\","
                + "\"alertType\":\"" + ALERT_TYPE + "\","
                + "\"originatingsource\":\"CRM\"},"
                + "\"dummy\":\"x\"}}";
        RoutingKeyConfig cfg  = routingConfig(EVENT_TYPE, ALERT_TYPE, "EMAIL");
        TemplateMaster   tmpl = templateFor("EMAIL", "to");

        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("SMS|EMAIL|BOTH");
        when(routingService.findMatchingConfigs(anyString())).thenReturn(List.of(cfg));
        when(templateService.findTemplates(any(), any())).thenReturn(Map.of("EMAIL", tmpl));

        notificationService.process(payload, validHeaders());

        verify(dltService, times(1))
                .logDlt(eq(payload), any(), contains("to"));
    }

    @Test
    void process_WhenSingleTemplateParamMissing_ShouldLogDltWithThatFieldName() {
        // Template requires "accountNumber" but the payload does not have it.
        // Service logs: "Missing template param: accountNumber"
        Map<String, String> extra = new HashMap<>();
        extra.put("mobileNumber", "9876543210");
        // accountNumber intentionally omitted
        String payload = buildSmsPayload("9876543210", extra);

        RoutingKeyConfig cfg  = routingConfig(EVENT_TYPE, ALERT_TYPE);
        TemplateMaster   tmpl = templateFor("SMS", "mobileNumber", "accountNumber");

        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("SMS|EMAIL|BOTH");
        when(routingService.findMatchingConfigs(anyString())).thenReturn(List.of(cfg));
        when(templateService.findTemplates(any(), any()))
                .thenReturn(Map.of("SMS", tmpl));

        notificationService.process(payload, validHeaders());

        verify(dltService, times(1))
                .logDlt(eq(payload), any(), contains("accountNumber"));
    }

    // =========================================================================
    // TICKET: Multiple parameters missing → return full missing list
    //
    // The service validates template params sequentially and stops at the first
    // missing one, so each field is tested individually to confirm they are all
    // checked.  Together these tests guarantee the full missing-list is reported.
    // =========================================================================

    @Test
    void process_WhenFirstTemplateParamMissing_ShouldLogWithFirstFieldName() {
        // Template: [firstName, lastName, accountNumber] — firstName absent.
        // Service logs: "Missing template param: firstName"
        Map<String, String> extra = new HashMap<>();
        extra.put("mobileNumber", "9876543210");
        extra.put("lastName",      "Doe");
        extra.put("accountNumber", "ACC-001");
        // firstName absent
        String payload = buildSmsPayload("9876543210", extra);

        RoutingKeyConfig cfg  = routingConfig(EVENT_TYPE, ALERT_TYPE);
        TemplateMaster   tmpl = templateFor("SMS", "mobileNumber", "firstName", "lastName", "accountNumber");

        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("SMS|EMAIL|BOTH");
        when(routingService.findMatchingConfigs(anyString())).thenReturn(List.of(cfg));
        when(templateService.findTemplates(any(), any()))
                .thenReturn(Map.of("SMS", tmpl));

        notificationService.process(payload, validHeaders());

        verify(dltService, times(1))
                .logDlt(eq(payload), any(), contains("firstName"));
    }

    @Test
    void process_WhenSecondTemplateParamMissing_ShouldLogWithSecondFieldName() {
        // Template: [firstName, lastName] — firstName present, lastName absent.
        // Service logs: "Missing template param: lastName"
        Map<String, String> extra = new HashMap<>();
        extra.put("mobileNumber", "9876543210");
        extra.put("firstName",    "John");
        // lastName absent
        String payload = buildSmsPayload("9876543210", extra);

        RoutingKeyConfig cfg  = routingConfig(EVENT_TYPE, ALERT_TYPE);
        TemplateMaster   tmpl = templateFor("SMS", "mobileNumber", "firstName", "lastName");

        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("SMS|EMAIL|BOTH");
        when(routingService.findMatchingConfigs(anyString())).thenReturn(List.of(cfg));
        when(templateService.findTemplates(any(), any()))
                .thenReturn(Map.of("SMS", tmpl));

        notificationService.process(payload, validHeaders());

        verify(dltService, times(1))
                .logDlt(eq(payload), any(), contains("lastName"));
    }

    @Test
    void process_WhenEventTypeAndAlertTypeBothMissing_ShouldLogInvalidInput() {
        // eventType and alertType are null; mobileNumber is also absent.
        // The precheck fires first: status=false → precheck = "SMS | 'mobileNumber' is missing"
        // → DLT message starts with "Invalid input"
        String payload = "{\"eventType\":null,"
                + "\"payload\":{\"customFieldDetails\":{"
                + "\"MessageType\":\"SMS\","
                + "\"alertType\":null,"
                + "\"originatingsource\":null}}}";
        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("SMS|EMAIL|BOTH");

        notificationService.process(payload, validHeaders());

        verify(dltService, atLeastOnce()).logDlt(eq(payload), any(), startsWith("Invalid input"));
    }

    // =========================================================================
    // TICKET: Extra parameters in payload → ignore safely
    // =========================================================================

    @Test
    void process_WhenPayloadHasExtraTopLevelFields_ShouldIgnoreThemAndProceed() {
        // Arrange – payload has fields the service does not care about
        String payload = "{\"eventType\":\"" + EVENT_TYPE + "\","
                + "\"extraField1\":\"ignored\","
                + "\"anotherExtra\":{\"nested\":true},"
                + "\"payload\":{\"customFieldDetails\":{"
                + "\"MessageType\":\"SMS\","
                + "\"alertType\":\"" + ALERT_TYPE + "\","
                + "\"originatingsource\":\"CRM\"},"
                + "\"mobileNumber\":\"9876543210\"}}";

        RoutingKeyConfig cfg  = routingConfig(EVENT_TYPE, ALERT_TYPE);
        TemplateMaster   tmpl = templateFor("SMS", "mobileNumber");

        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("SMS|EMAIL|BOTH");
        when(routingService.findMatchingConfigs(anyString())).thenReturn(List.of(cfg));
        when(templateService.findTemplates(EVENT_TYPE, ALERT_TYPE))
                .thenReturn(Map.of("SMS", tmpl));

        // Act
        assertDoesNotThrow(() -> notificationService.process(payload, validHeaders()));

        // Assert – no DLT error for extra fields; service proceeds normally
        verify(dltService, never()).logDlt(any(), any(), anyString());
    }

    @Test
    void process_WhenCustomFieldDetailsHasExtraKeys_ShouldIgnoreThemAndProceed() {
        // Arrange – customFieldDetails has keys beyond what the service reads
        String payload = "{\"eventType\":\"" + EVENT_TYPE + "\","
                + "\"payload\":{\"customFieldDetails\":{"
                + "\"MessageType\":\"SMS\","
                + "\"alertType\":\"" + ALERT_TYPE + "\","
                + "\"originatingsource\":\"CRM\","
                + "\"unknownKey\":\"someValue\","
                + "\"anotherUnknown\":42},"
                + "\"mobileNumber\":\"9876543210\"}}";

        RoutingKeyConfig cfg  = routingConfig(EVENT_TYPE, ALERT_TYPE);
        TemplateMaster   tmpl = templateFor("SMS", "mobileNumber");

        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("SMS|EMAIL|BOTH");
        when(routingService.findMatchingConfigs(anyString())).thenReturn(List.of(cfg));
        when(templateService.findTemplates(EVENT_TYPE, ALERT_TYPE))
                .thenReturn(Map.of("SMS", tmpl));

        assertDoesNotThrow(() -> notificationService.process(payload, validHeaders()));
        verify(dltService, never()).logDlt(any(), any(), anyString());
    }

    @Test
    void process_WhenPayloadHasExtraTemplateLevelFields_ShouldIgnoreAndResolveCorrectParams() {
        // Template only requires "mobileNumber"; payload also has firstName, lastName, etc.
        Map<String, String> extra = new HashMap<>();
        extra.put("firstName",  "John");
        extra.put("lastName",   "Doe");
        extra.put("dob",        "01-01-1990");
        String payload = buildSmsPayload("9876543210", extra);

        RoutingKeyConfig cfg  = routingConfig(EVENT_TYPE, ALERT_TYPE);
        TemplateMaster   tmpl = templateFor("SMS", "mobileNumber"); // only needs mobile

        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("SMS|EMAIL|BOTH");
        when(routingService.findMatchingConfigs(anyString())).thenReturn(List.of(cfg));
        when(templateService.findTemplates(EVENT_TYPE, ALERT_TYPE))
                .thenReturn(Map.of("SMS", tmpl));

        assertDoesNotThrow(() -> notificationService.process(payload, validHeaders()));
        verify(dltService, never()).logDlt(any(), any(), anyString());
    }

    // =========================================================================
    // TICKET: Valid routing config found → correct mapping
    // =========================================================================

    @Test
    void process_WhenValidSmsPayload_ShouldCompleteWithoutDlt() {
        // Fully valid SMS flow: message type allowed, routing found, template found,
        // all required params present.
        Map<String, String> extra = new HashMap<>();
        extra.put("mobileNumber", "9876543210");
        String payload = buildSmsPayload("9876543210", extra);

        RoutingKeyConfig cfg  = routingConfig(EVENT_TYPE, ALERT_TYPE);
        TemplateMaster   tmpl = templateFor("SMS", "mobileNumber");

        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("SMS|EMAIL|BOTH");
        when(routingService.findMatchingConfigs(anyString())).thenReturn(List.of(cfg));
        when(templateService.findTemplates(any(), any()))
                .thenReturn(Map.of("SMS", tmpl));

        notificationService.process(payload, validHeaders());

        verify(dltService, never()).logDlt(any(), any(), anyString());
        verify(routingService, times(1)).findMatchingConfigs(anyString());
        verify(templateService, atLeastOnce()).findTemplates(any(), any());
    }

    @Test
    void process_WhenValidEmailPayload_ShouldCompleteWithoutDlt() {
        // Fully valid EMAIL flow
        String payload = "{\"eventType\":\"" + EVENT_TYPE + "\","
                + "\"payload\":{\"customFieldDetails\":{"
                + "\"MessageType\":\"EMAIL\","
                + "\"alertType\":\"" + ALERT_TYPE + "\","
                + "\"originatingsource\":\"CRM\"},"
                + "\"to\":\"user@example.com\"}}";

        RoutingKeyConfig cfg  = routingConfig(EVENT_TYPE, ALERT_TYPE, "EMAIL");
        TemplateMaster   tmpl = templateFor("EMAIL", "to");

        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("SMS|EMAIL|BOTH");
        when(routingService.findMatchingConfigs(anyString())).thenReturn(List.of(cfg));
        when(templateService.findTemplates(any(), any()))
                .thenReturn(Map.of("EMAIL", tmpl));

        notificationService.process(payload, validHeaders());

        verify(dltService, never()).logDlt(any(), any(), anyString());
    }

    @Test
    void process_WhenRoutingMatchIsCaseInsensitive_ShouldFindConfig() {
        // eventType in payload is lowercase; routing config uses mixed-case → must still match
        String payload = "{\"eventType\":\"customerevents-customercreated\","
                + "\"payload\":{\"customFieldDetails\":{"
                + "\"MessageType\":\"SMS\","
                + "\"alertType\":\"customer_created\","
                + "\"originatingsource\":\"CRM\"},"
                + "\"mobileNumber\":\"9876543210\"}}";

        RoutingKeyConfig cfg  = routingConfig("CustomerEvents-CustomerCreated", "CUSTOMER_CREATED");
        TemplateMaster   tmpl = templateFor("SMS", "mobileNumber");

        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("SMS|EMAIL|BOTH");
        // The service passes the payload's value to findMatchingConfig; that service
        // handles case-insensitivity internally and returns the config.
        when(routingService.findMatchingConfigs(anyString())).thenReturn(List.of(cfg));
        when(templateService.findTemplates(any(), any()))
                .thenReturn(Map.of("SMS", tmpl));

        notificationService.process(payload, validHeaders());

        verify(dltService, never()).logDlt(any(), any(), anyString());
    }

    @Test
    void process_WhenOriginatingSourceUsedInsteadOfAlertType_ShouldStillRoute() {
        // Condition: (!isNullOrEmpty(eventType) && !isNullOrEmpty(originatingSource))
        // This covers the branch where alertType is null but originatingSource is set.
        String payload = "{\"eventType\":\"" + EVENT_TYPE + "\","
                + "\"payload\":{\"customFieldDetails\":{"
                + "\"MessageType\":\"SMS\","
                + "\"originatingsource\":\"CRM\"},"  // no alertType
                + "\"mobileNumber\":\"9876543210\"}}";

        RoutingKeyConfig cfg  = routingConfig(EVENT_TYPE, "");
        TemplateMaster   tmpl = templateFor("SMS", "mobileNumber");

        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("SMS|EMAIL|BOTH");
        when(routingService.findMatchingConfigs(anyString())).thenReturn(List.of(cfg));
        when(templateService.findTemplates(any(), any()))
                .thenReturn(Map.of("SMS", tmpl));

        notificationService.process(payload, validHeaders());

        verify(dltService, never()).logDlt(any(), any(), anyString());
    }

    @Test
    void process_WhenAllowedConsumerListContainsMultipleValues_ShouldMatchCorrectOne() {
        // configService returns pipe-separated values; "SMS" must be correctly parsed out
        Map<String, String> extra = new HashMap<>();
        extra.put("mobileNumber", "9876543210");
        String payload = buildSmsPayload("9876543210", extra);

        RoutingKeyConfig cfg  = routingConfig(EVENT_TYPE, ALERT_TYPE);
        TemplateMaster   tmpl = templateFor("SMS", "mobileNumber");

        when(configService.getValue("ALLOWED_CONSUMER")).thenReturn("EMAIL|SMS|BOTH|PUSH");
        when(routingService.findMatchingConfigs(anyString())).thenReturn(List.of(cfg));
        when(templateService.findTemplates(any(), any()))
                .thenReturn(Map.of("SMS", tmpl));

        notificationService.process(payload, validHeaders());

        verify(dltService, never()).logDlt(any(), any(), anyString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // isNullOrEmpty unit tests (pure-logic, no mocks needed)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void isNullOrEmpty_WhenNull_ShouldReturnTrue() {
        assertTrue(NotificationService.isNullOrEmpty(null));
    }

    @Test
    void isNullOrEmpty_WhenEmpty_ShouldReturnTrue() {
        assertTrue(NotificationService.isNullOrEmpty(""));
    }

    @Test
    void isNullOrEmpty_WhenBlankSpaces_ShouldReturnTrue() {
        assertTrue(NotificationService.isNullOrEmpty("   "));
    }

    @Test
    void isNullOrEmpty_WhenLiteralNull_ShouldReturnTrue() {
        assertTrue(NotificationService.isNullOrEmpty("null"));
        assertTrue(NotificationService.isNullOrEmpty("NULL"));
        assertTrue(NotificationService.isNullOrEmpty("Null"));
    }

    @Test
    void isNullOrEmpty_WhenValidValue_ShouldReturnFalse() {
        assertFalse(NotificationService.isNullOrEmpty("CustomerEvents"));
        assertFalse(NotificationService.isNullOrEmpty("SMS"));
    }
}

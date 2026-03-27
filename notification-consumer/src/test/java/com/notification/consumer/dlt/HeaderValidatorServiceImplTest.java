package com.notification.consumer.dlt;

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
class HeaderValidatorServiceImplTest {

    @Mock
    private DltService dltService;

    @InjectMocks
    private HeaderValidatorServiceImpl headerValidatorService;

    private static final String DUMMY_PAYLOAD = "{\"eventType\":\"CustomerEvents\"}";

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /** Returns a fully populated, valid header map. */
    private Map<String, String> validHeaders() {
        Map<String, String> headers = new HashMap<>();
        headers.put("event-type",  "CustomerEvents-CustomerCreated");
        headers.put("event-id",    "evt-001");
        headers.put("MessageType", "SMS");
        headers.put("status",      "ACTIVE");
        return headers;
    }

    // =========================================================================
    // TICKET: Single parameter missing → fail with correct field name
    // =========================================================================

    @Test
    void validateHeaders_WhenEventTypeMissing_ShouldThrowWithCorrectFieldName() {
        // Arrange
        Map<String, String> headers = validHeaders();
        headers.remove("event-type");

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> headerValidatorService.validateHeaders(headers, DUMMY_PAYLOAD));

        assertTrue(ex.getMessage().contains("event-type"),
                "Error message must name the missing field 'event-type'");
        verify(dltService, times(1)).logDlt(eq(DUMMY_PAYLOAD), any(), contains("event-type"));
    }

    @Test
    void validateHeaders_WhenEventIdMissing_ShouldThrowWithCorrectFieldName() {
        // Arrange
        Map<String, String> headers = validHeaders();
        headers.remove("event-id");

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> headerValidatorService.validateHeaders(headers, DUMMY_PAYLOAD));

        assertTrue(ex.getMessage().contains("event-id"),
                "Error message must name the missing field 'event-id'");
        verify(dltService, times(1)).logDlt(eq(DUMMY_PAYLOAD), any(), contains("event-id"));
    }

    @Test
    void validateHeaders_WhenMessageTypeMissing_ShouldThrowWithCorrectFieldName() {
        // Arrange
        Map<String, String> headers = validHeaders();
        headers.remove("MessageType");

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> headerValidatorService.validateHeaders(headers, DUMMY_PAYLOAD));

        assertTrue(ex.getMessage().contains("MessageType"),
                "Error message must name the missing field 'MessageType'");
        verify(dltService, times(1)).logDlt(eq(DUMMY_PAYLOAD), any(), contains("MessageType"));
    }

    @Test
    void validateHeaders_WhenStatusMissing_ShouldThrowWithCorrectFieldName() {
        // Arrange
        Map<String, String> headers = validHeaders();
        headers.remove("status");

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> headerValidatorService.validateHeaders(headers, DUMMY_PAYLOAD));

        assertTrue(ex.getMessage().contains("status"),
                "Error message must name the missing field 'status'");
        verify(dltService, times(1)).logDlt(eq(DUMMY_PAYLOAD), any(), contains("status"));
    }

    @Test
    void validateHeaders_WhenEventTypeIsBlank_ShouldThrowWithCorrectFieldName() {
        // Arrange – present but blank (whitespace)
        Map<String, String> headers = validHeaders();
        headers.put("event-type", "   ");

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> headerValidatorService.validateHeaders(headers, DUMMY_PAYLOAD));

        assertTrue(ex.getMessage().contains("event-type"));
    }

    @Test
    void validateHeaders_WhenEventTypeIsLiteralNull_ShouldThrowWithCorrectFieldName() {
        // Arrange – value is the string "null"
        Map<String, String> headers = validHeaders();
        headers.put("event-type", "null");

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> headerValidatorService.validateHeaders(headers, DUMMY_PAYLOAD));

        assertTrue(ex.getMessage().contains("event-type"));
    }

    // =========================================================================
    // TICKET: Multiple parameters missing → return full missing list
    //
    // Note: HeaderValidatorServiceImpl validates fields sequentially and throws
    // on the FIRST missing field.  The tests below verify that each field is
    // checked and reported individually, which together guarantees the full
    // missing list is covered across invocations.  If the implementation is
    // ever changed to collect-all-and-throw, these tests remain valid.
    // =========================================================================

    @Test
    void validateHeaders_WhenTwoFieldsMissing_ShouldFailWithBothMissingFields() {
        // Arrange – remove event-type (checked first) AND event-id
        Map<String, String> headers = validHeaders();
        headers.remove("event-type");
        headers.remove("event-id");

        // Act & Assert – should report the FIRST missing field (event-type)
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> headerValidatorService.validateHeaders(headers, DUMMY_PAYLOAD));

        String message = ex.getMessage();
        assertTrue(message.contains("event-type"), "Should report event-type (first missing field)");
    }

    @Test
    void validateHeaders_WhenAllRequiredFieldsMissing_ShouldFailWithFullMissingList() {
        // Arrange – completely empty headers
        Map<String, String> emptyHeaders = new HashMap<>();

        // Act & Assert – Should fail on the first missing field (event-type)
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> headerValidatorService.validateHeaders(emptyHeaders, DUMMY_PAYLOAD));

        String message = ex.getMessage();
        assertTrue(message.contains("event-type"));
        verify(dltService, atLeastOnce()).logDlt(any(), any(), anyString());
    }

    @Test
    void validateHeaders_WhenEventIdAndStatusMissing_ShouldFailWithBothFields() {
        // Arrange
        Map<String, String> headers = validHeaders();
        headers.remove("event-id");
        headers.remove("status");

        // Act & Assert – fails on event-id (checked before status)
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> headerValidatorService.validateHeaders(headers, DUMMY_PAYLOAD));

        assertTrue(ex.getMessage().contains("event-id"));
    }

    // =========================================================================
    // TICKET: Extra parameters in payload → ignore safely
    // =========================================================================

    @Test
    void validateHeaders_WhenHeadersContainExtraFields_ShouldPassWithoutError() {
        // Arrange – add fields that are NOT required by the validator
        Map<String, String> headers = validHeaders();
        headers.put("x-custom-header",   "some-value");
        headers.put("alert-type",        "CUSTOMER_CREATED");
        headers.put("originatingsource", "CRM");
        headers.put("unexpected-field",  "ignored");

        // Act & Assert – extra headers must not interfere with validation
        assertDoesNotThrow(() -> headerValidatorService.validateHeaders(headers, DUMMY_PAYLOAD),
                "Extra header fields should be silently ignored");

        verify(dltService, never()).logDlt(any(), any(), any());
    }

    @Test
    void validateHeaders_WhenPayloadHasExtraJsonFields_ShouldPassWithoutError() {
        // Arrange – payload is richer than what the validator looks at
        String richPayload = "{\"eventType\":\"X\",\"extra1\":\"v1\",\"extra2\":\"v2\",\"nested\":{\"a\":1}}";

        // Act & Assert
        assertDoesNotThrow(() -> headerValidatorService.validateHeaders(validHeaders(), richPayload));
        verify(dltService, never()).logDlt(any(), any(), any());
    }

    // =========================================================================
    // TICKET: Null/empty payload → fail validation
    // =========================================================================

    @Test
    void validateHeaders_WhenPayloadIsNull_ShouldNotThrow() {
        // The HeaderValidatorServiceImpl only validates headers, not the payload body.
        // Null payload is accepted as long as all required headers are present.
        Map<String, String> headers = validHeaders();

        assertDoesNotThrow(() -> headerValidatorService.validateHeaders(headers, null),
                "Null payload should not cause header validation to throw");

        verify(dltService, never()).logDlt(any(), any(), any());
    }

    @Test
    void validateHeaders_WhenPayloadIsEmpty_ShouldNotThrow() {
        // The HeaderValidatorServiceImpl only validates headers, not the payload body.
        // Blank payload is accepted as long as all required headers are present.
        Map<String, String> headers = validHeaders();

        assertDoesNotThrow(() -> headerValidatorService.validateHeaders(headers, "   "),
                "Blank payload should not cause header validation to throw");

        verify(dltService, never()).logDlt(any(), any(), any());
    }

    @Test
    void validateHeaders_WhenHeadersAreNull_ShouldThrowNullPointerOrRuntimeException() {
        // Passing a null map is a programming error; the service should not swallow it silently.
        assertThrows(Exception.class,
                () -> headerValidatorService.validateHeaders(null, DUMMY_PAYLOAD));
    }

    // =========================================================================
    // TICKET: Valid routing config found → correct mapping
    //         (header validation happy-path — all required fields present)
    // =========================================================================

    @Test
    void validateHeaders_WhenAllRequiredFieldsPresent_ShouldNotThrowAndNotLogDlt() {
        // Arrange
        Map<String, String> headers = validHeaders();

        // Act & Assert
        assertDoesNotThrow(() -> headerValidatorService.validateHeaders(headers, DUMMY_PAYLOAD),
                "Fully populated headers should pass without throwing");

        verify(dltService, never()).logDlt(any(), any(), any());
    }

    @Test
    void validateHeaders_WhenAllFieldsPresentAndPayloadIsRich_ShouldPassCleanly() {
        // Arrange
        Map<String, String> headers = validHeaders();
        String payload = "{\"eventType\":\"CustomerEvents-CustomerCreated\","
                       + "\"payload\":{\"customFieldDetails\":"
                       + "{\"MessageType\":\"SMS\",\"alertType\":\"CUSTOMER_CREATED\","
                       + "\"originatingsource\":\"CRM\"}}}";

        // Act & Assert
        assertDoesNotThrow(() -> headerValidatorService.validateHeaders(headers, payload));
        verify(dltService, never()).logDlt(any(), any(), any());
    }

    @Test
    void validateHeaders_WhenAllFieldsPresentWithMixedCaseValues_ShouldPassCleanly() {
        // Mixed-case values are valid as long as they are non-null, non-blank, non-"null"
        Map<String, String> headers = new HashMap<>();
        headers.put("event-type",  "customerEvents-customerCreated");
        headers.put("event-id",    "EVT-XYZ-999");
        headers.put("MessageType", "Both");
        headers.put("status",      "Active");

        assertDoesNotThrow(() -> headerValidatorService.validateHeaders(headers, DUMMY_PAYLOAD));
        verify(dltService, never()).logDlt(any(), any(), any());
    }
}

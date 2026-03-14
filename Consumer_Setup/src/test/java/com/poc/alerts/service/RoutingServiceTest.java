package com.poc.alerts.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.poc.alerts.entity.KeyRoutingConfig;
import com.poc.alerts.repository.KeyRoutingConfigRepository;

@ExtendWith(MockitoExtension.class)
class RoutingServiceTest {

    @Mock
    private KeyRoutingConfigRepository repository;

    private RoutingService routingService;

    @BeforeEach
    void setUp() {
        routingService = new RoutingService(repository);
    }

    @Test
    void testGetRoutingConfig_Success() {
        String messageType = "SMS";
        String alertType = "FUND_TRANSFER";
        KeyRoutingConfig mockConfig = new KeyRoutingConfig();
        mockConfig.setMessageType(messageType);
        mockConfig.setAlertType(alertType);
        mockConfig.setIsActive(true);

        when(repository.findByMessageTypeAndAlertTypeAndIsActive(messageType, alertType, true))
                .thenReturn(Optional.of(mockConfig));

        KeyRoutingConfig result = routingService.getRoutingConfig(messageType, alertType);

        assertNotNull(result);
        assertEquals(messageType, result.getMessageType());
    }

    @Test
    void testGetRoutingConfig_NotFound() {
        String messageType = "SMS";
        String alertType = "NONE";

        when(repository.findByMessageTypeAndAlertTypeAndIsActive(messageType, alertType, true))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> routingService.getRoutingConfig(messageType, alertType));
    }
}

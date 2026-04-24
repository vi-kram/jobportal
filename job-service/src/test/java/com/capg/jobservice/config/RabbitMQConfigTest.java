package com.capg.jobservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import static org.junit.jupiter.api.Assertions.*;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    void exchange_isNotNull() {
        assertNotNull(config.jobportalExchange());
        assertEquals(RabbitMQConfig.EXCHANGE, config.jobportalExchange().getName());
    }

    @Test
    void deadLetterExchange_isNotNull() {
        assertNotNull(config.deadLetterExchange());
        assertEquals(RabbitMQConfig.DLX, config.deadLetterExchange().getName());
    }

    @Test
    void queues_areNotNull() {
        assertNotNull(config.jobCreatedAnalyticsQueue());
        assertNotNull(config.jobCreatedNotifyQueue());
        assertNotNull(config.jobCreatedSearchQueue());
        assertNotNull(config.jobClosedAnalyticsQueue());
        assertNotNull(config.jobClosedNotifyQueue());
    }

    @Test
    void dlqs_areNotNull() {
        assertNotNull(config.jobCreatedAnalyticsDlq());
        assertNotNull(config.jobCreatedNotifyDlq());
        assertNotNull(config.jobCreatedSearchDlq());
        assertNotNull(config.jobClosedAnalyticsDlq());
        assertNotNull(config.jobClosedNotifyDlq());
    }

    @Test
    void bindings_areNotNull() {
        assertNotNull(config.jobCreatedAnalyticsBinding());
        assertNotNull(config.jobCreatedNotifyBinding());
        assertNotNull(config.jobCreatedSearchBinding());
        assertNotNull(config.jobClosedAnalyticsBinding());
        assertNotNull(config.jobClosedNotifyBinding());
    }

    @Test
    void dlqBindings_areNotNull() {
        assertNotNull(config.jobCreatedAnalyticsDlqBinding());
        assertNotNull(config.jobCreatedNotifyDlqBinding());
        assertNotNull(config.jobCreatedSearchDlqBinding());
        assertNotNull(config.jobClosedAnalyticsDlqBinding());
        assertNotNull(config.jobClosedNotifyDlqBinding());
    }

    @Test
    void messageConverter_isNotNull() {
        Jackson2JsonMessageConverter converter = config.messageConverter();
        assertNotNull(converter);
    }
}

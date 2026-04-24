package com.capg.applicationservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import static org.junit.jupiter.api.Assertions.*;

class RabbitMQConfigTest {

    private final RabbitMQConfig config = new RabbitMQConfig();

    @Test
    void exchange_isNotNull() {
        assertNotNull(config.exchange());
        assertEquals(RabbitMQConfig.EXCHANGE, config.exchange().getName());
    }

    @Test
    void deadLetterExchange_isNotNull() {
        assertNotNull(config.deadLetterExchange());
        assertEquals(RabbitMQConfig.DLX, config.deadLetterExchange().getName());
    }

    @Test
    void queues_areNotNull() {
        assertNotNull(config.jobAppliedAnalyticsQueue());
        assertNotNull(config.jobAppliedNotifyQueue());
    }

    @Test
    void dlqs_areNotNull() {
        assertNotNull(config.jobAppliedAnalyticsDlq());
        assertNotNull(config.jobAppliedNotifyDlq());
    }

    @Test
    void bindings_areNotNull() {
        assertNotNull(config.jobAppliedAnalyticsBinding());
        assertNotNull(config.jobAppliedNotifyBinding());
    }

    @Test
    void dlqBindings_areNotNull() {
        assertNotNull(config.jobAppliedAnalyticsDlqBinding());
        assertNotNull(config.jobAppliedNotifyDlqBinding());
    }

    @Test
    void messageConverter_isNotNull() {
        Jackson2JsonMessageConverter converter = config.messageConverter();
        assertNotNull(converter);
    }
}

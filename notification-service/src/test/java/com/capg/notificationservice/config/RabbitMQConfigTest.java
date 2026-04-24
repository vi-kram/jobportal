package com.capg.notificationservice.config;

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
        assertNotNull(config.jobCreatedQueue());
        assertNotNull(config.jobAppliedQueue());
        assertNotNull(config.jobClosedQueue());
        assertNotNull(config.resumeUploadQueue());
    }

    @Test
    void dlqs_areNotNull() {
        assertNotNull(config.jobCreatedDlq());
        assertNotNull(config.jobAppliedDlq());
        assertNotNull(config.jobClosedDlq());
        assertNotNull(config.resumeUploadDlq());
    }

    @Test
    void bindings_areNotNull() {
        assertNotNull(config.jobCreatedBinding());
        assertNotNull(config.jobAppliedBinding());
        assertNotNull(config.jobClosedBinding());
        assertNotNull(config.resumeBinding());
    }

    @Test
    void dlqBindings_areNotNull() {
        assertNotNull(config.jobCreatedDlqBinding());
        assertNotNull(config.jobAppliedDlqBinding());
        assertNotNull(config.jobClosedDlqBinding());
        assertNotNull(config.resumeUploadDlqBinding());
    }

    @Test
    void messageConverter_isNotNull() {
        Jackson2JsonMessageConverter converter = config.messageConverter();
        assertNotNull(converter);
    }
}

package com.capg.analyticsservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.support.converter.MessageConverter;
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
        assertNotNull(config.resumeUploadQueue());
        assertNotNull(config.jobClosedQueue());
    }

    @Test
    void dlqs_areNotNull() {
        assertNotNull(config.jobCreatedDlq());
        assertNotNull(config.jobAppliedDlq());
        assertNotNull(config.resumeUploadDlq());
        assertNotNull(config.jobClosedDlq());
    }

    @Test
    void bindings_areNotNull() {
        assertNotNull(config.jobCreatedBinding());
        assertNotNull(config.jobAppliedBinding());
        assertNotNull(config.resumeUploadBinding());
        assertNotNull(config.jobClosedBinding());
    }

    @Test
    void dlqBindings_areNotNull() {
        assertNotNull(config.jobCreatedDlqBinding());
        assertNotNull(config.jobAppliedDlqBinding());
        assertNotNull(config.resumeUploadDlqBinding());
        assertNotNull(config.jobClosedDlqBinding());
    }

    @Test
    void messageConverter_isNotNull() {
        MessageConverter converter = config.jsonMessageConverter();
        assertNotNull(converter);
    }
}

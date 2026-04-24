package com.capg.searchservice.config;

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
        assertNotNull(config.jobClosedQueue());
    }

    @Test
    void dlqs_areNotNull() {
        assertNotNull(config.jobCreatedDlq());
        assertNotNull(config.jobClosedDlq());
    }

    @Test
    void bindings_areNotNull() {
        assertNotNull(config.jobCreatedBinding());
        assertNotNull(config.jobClosedBinding());
    }

    @Test
    void dlqBindings_areNotNull() {
        assertNotNull(config.jobCreatedDlqBinding());
        assertNotNull(config.jobClosedDlqBinding());
    }

    @Test
    void messageConverter_isNotNull() {
        Jackson2JsonMessageConverter converter = config.messageConverter();
        assertNotNull(converter);
    }
}

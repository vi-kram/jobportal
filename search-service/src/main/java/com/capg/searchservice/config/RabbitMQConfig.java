package com.capg.searchservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE          = "jobportal.exchange";
    public static final String DLX               = "jobportal.dlx";
    public static final String JOB_CREATED_QUEUE = "job.created.search.queue";
    public static final String JOB_CLOSED_QUEUE  = "job.closed.search.queue";

    @Bean public TopicExchange exchange() { return new TopicExchange(EXCHANGE); }
    @Bean public DirectExchange deadLetterExchange() { return new DirectExchange(DLX); }

    // ── Main Queues ──────────────────────────────────────────────────────────

    @Bean public Queue jobCreatedQueue() {
        return QueueBuilder.durable(JOB_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", JOB_CREATED_QUEUE + ".dlq")
                .build();
    }

    @Bean public Queue jobClosedQueue() {
        return QueueBuilder.durable(JOB_CLOSED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", JOB_CLOSED_QUEUE + ".dlq")
                .build();
    }

    // ── DLQs ────────────────────────────────────────────────────────────────

    @Bean public Queue jobCreatedDlq() { return new Queue(JOB_CREATED_QUEUE + ".dlq"); }
    @Bean public Queue jobClosedDlq()  { return new Queue(JOB_CLOSED_QUEUE + ".dlq"); }

    // ── Main Bindings ────────────────────────────────────────────────────────

    @Bean public Binding jobCreatedBinding() {
        return BindingBuilder.bind(jobCreatedQueue()).to(exchange()).with("job.created");
    }

    @Bean public Binding jobClosedBinding() {
        return BindingBuilder.bind(jobClosedQueue()).to(exchange()).with("job.closed");
    }

    // ── DLQ Bindings ─────────────────────────────────────────────────────────

    @Bean public Binding jobCreatedDlqBinding() {
        return BindingBuilder.bind(jobCreatedDlq()).to(deadLetterExchange()).with(JOB_CREATED_QUEUE + ".dlq");
    }

    @Bean public Binding jobClosedDlqBinding() {
        return BindingBuilder.bind(jobClosedDlq()).to(deadLetterExchange()).with(JOB_CLOSED_QUEUE + ".dlq");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

package com.capg.resumeservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE   = "jobportal.exchange";
    public static final String DLX        = "jobportal.dlx";
    public static final String RESUME_KEY = "resume.uploaded";

    private static final String DLQ_ANALYTICS = "resume.upload.analytics.queue.dlq";
    private static final String DLQ_NOTIFY    = "resume.upload.notify.queue.dlq";

    @Bean public TopicExchange jobportalExchange() { return new TopicExchange(EXCHANGE); }
    @Bean public DirectExchange deadLetterExchange() { return new DirectExchange(DLX); }

    // ── Main Queues ──────────────────────────────────────────────────────────

    @Bean public Queue resumeAnalyticsQueue() {
        return QueueBuilder.durable("resume.upload.analytics.queue")
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_ANALYTICS)
                .build();
    }
    @Bean public Queue resumeNotifyQueue() {
        return QueueBuilder.durable("resume.upload.notify.queue")
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_NOTIFY)
                .build();
    }

    // ── DLQs ────────────────────────────────────────────────────────────────

    @Bean public Queue resumeAnalyticsDlq() { return new Queue(DLQ_ANALYTICS); }
    @Bean public Queue resumeNotifyDlq()    { return new Queue(DLQ_NOTIFY); }

    // ── Main Bindings ────────────────────────────────────────────────────────

    @Bean public Binding resumeAnalyticsBinding() {
        return BindingBuilder.bind(resumeAnalyticsQueue()).to(jobportalExchange()).with(RESUME_KEY);
    }
    @Bean public Binding resumeNotifyBinding() {
        return BindingBuilder.bind(resumeNotifyQueue()).to(jobportalExchange()).with(RESUME_KEY);
    }

    // ── DLQ Bindings ─────────────────────────────────────────────────────────

    @Bean public Binding resumeAnalyticsDlqBinding() {
        return BindingBuilder.bind(resumeAnalyticsDlq()).to(deadLetterExchange()).with(DLQ_ANALYTICS);
    }
    @Bean public Binding resumeNotifyDlqBinding() {
        return BindingBuilder.bind(resumeNotifyDlq()).to(deadLetterExchange()).with(DLQ_NOTIFY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

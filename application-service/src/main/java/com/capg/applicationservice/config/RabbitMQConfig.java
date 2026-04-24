package com.capg.applicationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE        = "jobportal.exchange";
    public static final String DLX             = "jobportal.dlx";
    public static final String JOB_APPLIED_KEY = "job.applied";

    private static final String DLQ_ANALYTICS = "job.applied.analytics.queue.dlq";
    private static final String DLQ_NOTIFY    = "job.applied.notify.queue.dlq";

    @Bean public TopicExchange jobportalExchange() { return new TopicExchange(EXCHANGE); }
    @Bean public DirectExchange deadLetterExchange() { return new DirectExchange(DLX); }

    // ── Main Queues ──────────────────────────────────────────────────────────

    @Bean public Queue jobAppliedAnalyticsQueue() {
        return QueueBuilder.durable("job.applied.analytics.queue")
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_ANALYTICS)
                .build();
    }
    @Bean public Queue jobAppliedNotifyQueue() {
        return QueueBuilder.durable("job.applied.notify.queue")
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_NOTIFY)
                .build();
    }

    // ── DLQs ────────────────────────────────────────────────────────────────

    @Bean public Queue jobAppliedAnalyticsDlq() { return new Queue(DLQ_ANALYTICS); }
    @Bean public Queue jobAppliedNotifyDlq()    { return new Queue(DLQ_NOTIFY); }

    // ── Main Bindings ────────────────────────────────────────────────────────

    @Bean public Binding jobAppliedAnalyticsBinding() {
        return BindingBuilder.bind(jobAppliedAnalyticsQueue()).to(jobportalExchange()).with(JOB_APPLIED_KEY);
    }
    @Bean public Binding jobAppliedNotifyBinding() {
        return BindingBuilder.bind(jobAppliedNotifyQueue()).to(jobportalExchange()).with(JOB_APPLIED_KEY);
    }

    // ── DLQ Bindings ─────────────────────────────────────────────────────────

    @Bean public Binding jobAppliedAnalyticsDlqBinding() {
        return BindingBuilder.bind(jobAppliedAnalyticsDlq()).to(deadLetterExchange()).with(DLQ_ANALYTICS);
    }
    @Bean public Binding jobAppliedNotifyDlqBinding() {
        return BindingBuilder.bind(jobAppliedNotifyDlq()).to(deadLetterExchange()).with(DLQ_NOTIFY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

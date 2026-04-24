package com.capg.jobservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE        = "jobportal.exchange";
    public static final String DLX             = "jobportal.dlx";
    public static final String JOB_CREATED_KEY = "job.created";
    public static final String JOB_CLOSED_KEY  = "job.closed";

    private static final String DLQ_CREATED_ANALYTICS = "job.created.analytics.queue.dlq";
    private static final String DLQ_CREATED_NOTIFY    = "job.created.notify.queue.dlq";
    private static final String DLQ_CREATED_SEARCH    = "job.created.search.queue.dlq";
    private static final String DLQ_CLOSED_ANALYTICS  = "job.closed.analytics.queue.dlq";
    private static final String DLQ_CLOSED_NOTIFY     = "job.closed.notify.queue.dlq";

    @Bean public TopicExchange jobportalExchange() { return new TopicExchange(EXCHANGE); }
    @Bean public DirectExchange deadLetterExchange() { return new DirectExchange(DLX); }

    // ── Main Queues ──────────────────────────────────────────────────────────

    @Bean public Queue jobCreatedAnalyticsQueue() {
        return QueueBuilder.durable("job.created.analytics.queue")
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_CREATED_ANALYTICS)
                .build();
    }
    @Bean public Queue jobCreatedNotifyQueue() {
        return QueueBuilder.durable("job.created.notify.queue")
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_CREATED_NOTIFY)
                .build();
    }
    @Bean public Queue jobCreatedSearchQueue() {
        return QueueBuilder.durable("job.created.search.queue")
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_CREATED_SEARCH)
                .build();
    }
    @Bean public Queue jobClosedAnalyticsQueue() {
        return QueueBuilder.durable("job.closed.analytics.queue")
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_CLOSED_ANALYTICS)
                .build();
    }
    @Bean public Queue jobClosedNotifyQueue() {
        return QueueBuilder.durable("job.closed.notify.queue")
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DLQ_CLOSED_NOTIFY)
                .build();
    }

    // ── DLQs ────────────────────────────────────────────────────────────────

    @Bean public Queue jobCreatedAnalyticsDlq() { return new Queue(DLQ_CREATED_ANALYTICS); }
    @Bean public Queue jobCreatedNotifyDlq()    { return new Queue(DLQ_CREATED_NOTIFY); }
    @Bean public Queue jobCreatedSearchDlq()    { return new Queue(DLQ_CREATED_SEARCH); }
    @Bean public Queue jobClosedAnalyticsDlq()  { return new Queue(DLQ_CLOSED_ANALYTICS); }
    @Bean public Queue jobClosedNotifyDlq()     { return new Queue(DLQ_CLOSED_NOTIFY); }

    // ── Main Bindings ────────────────────────────────────────────────────────

    @Bean public Binding jobCreatedAnalyticsBinding() {
        return BindingBuilder.bind(jobCreatedAnalyticsQueue()).to(jobportalExchange()).with(JOB_CREATED_KEY);
    }
    @Bean public Binding jobCreatedNotifyBinding() {
        return BindingBuilder.bind(jobCreatedNotifyQueue()).to(jobportalExchange()).with(JOB_CREATED_KEY);
    }
    @Bean public Binding jobCreatedSearchBinding() {
        return BindingBuilder.bind(jobCreatedSearchQueue()).to(jobportalExchange()).with(JOB_CREATED_KEY);
    }
    @Bean public Binding jobClosedAnalyticsBinding() {
        return BindingBuilder.bind(jobClosedAnalyticsQueue()).to(jobportalExchange()).with(JOB_CLOSED_KEY);
    }
    @Bean public Binding jobClosedNotifyBinding() {
        return BindingBuilder.bind(jobClosedNotifyQueue()).to(jobportalExchange()).with(JOB_CLOSED_KEY);
    }

    // ── DLQ Bindings ─────────────────────────────────────────────────────────

    @Bean public Binding jobCreatedAnalyticsDlqBinding() {
        return BindingBuilder.bind(jobCreatedAnalyticsDlq()).to(deadLetterExchange()).with(DLQ_CREATED_ANALYTICS);
    }
    @Bean public Binding jobCreatedNotifyDlqBinding() {
        return BindingBuilder.bind(jobCreatedNotifyDlq()).to(deadLetterExchange()).with(DLQ_CREATED_NOTIFY);
    }
    @Bean public Binding jobCreatedSearchDlqBinding() {
        return BindingBuilder.bind(jobCreatedSearchDlq()).to(deadLetterExchange()).with(DLQ_CREATED_SEARCH);
    }
    @Bean public Binding jobClosedAnalyticsDlqBinding() {
        return BindingBuilder.bind(jobClosedAnalyticsDlq()).to(deadLetterExchange()).with(DLQ_CLOSED_ANALYTICS);
    }
    @Bean public Binding jobClosedNotifyDlqBinding() {
        return BindingBuilder.bind(jobClosedNotifyDlq()).to(deadLetterExchange()).with(DLQ_CLOSED_NOTIFY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

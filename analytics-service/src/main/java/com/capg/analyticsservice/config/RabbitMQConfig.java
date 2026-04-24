package com.capg.analyticsservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE            = "jobportal.exchange";
    public static final String DLX                 = "jobportal.dlx";
    public static final String JOB_CREATED_QUEUE   = "job.created.analytics.queue";
    public static final String JOB_APPLIED_QUEUE   = "job.applied.analytics.queue";
    public static final String RESUME_UPLOAD_QUEUE = "resume.upload.analytics.queue";
    public static final String JOB_CLOSED_QUEUE    = "job.closed.analytics.queue";

    public static final String JOB_CREATED_KEY    = "job.created";
    public static final String JOB_APPLIED_KEY    = "job.applied";
    public static final String RESUME_UPLOAD_KEY  = "resume.uploaded";
    public static final String JOB_CLOSED_KEY     = "job.closed";

    @Bean public TopicExchange jobportalExchange() { return new TopicExchange(EXCHANGE); }
    @Bean public DirectExchange deadLetterExchange() { return new DirectExchange(DLX); }

    // ── Main Queues ──────────────────────────────────────────────────────────

    @Bean public Queue jobCreatedQueue() {
        return QueueBuilder.durable(JOB_CREATED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", JOB_CREATED_QUEUE + ".dlq")
                .build();
    }
    @Bean public Queue jobAppliedQueue() {
        return QueueBuilder.durable(JOB_APPLIED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", JOB_APPLIED_QUEUE + ".dlq")
                .build();
    }
    @Bean public Queue resumeUploadQueue() {
        return QueueBuilder.durable(RESUME_UPLOAD_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", RESUME_UPLOAD_QUEUE + ".dlq")
                .build();
    }
    @Bean public Queue jobClosedQueue() {
        return QueueBuilder.durable(JOB_CLOSED_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", JOB_CLOSED_QUEUE + ".dlq")
                .build();
    }

    // ── DLQs ────────────────────────────────────────────────────────────────

    @Bean public Queue jobCreatedDlq()   { return new Queue(JOB_CREATED_QUEUE + ".dlq"); }
    @Bean public Queue jobAppliedDlq()   { return new Queue(JOB_APPLIED_QUEUE + ".dlq"); }
    @Bean public Queue resumeUploadDlq() { return new Queue(RESUME_UPLOAD_QUEUE + ".dlq"); }
    @Bean public Queue jobClosedDlq()    { return new Queue(JOB_CLOSED_QUEUE + ".dlq"); }

    // ── Main Bindings ────────────────────────────────────────────────────────

    @Bean public Binding jobCreatedBinding() {
        return BindingBuilder.bind(jobCreatedQueue()).to(jobportalExchange()).with(JOB_CREATED_KEY);
    }
    @Bean public Binding jobAppliedBinding() {
        return BindingBuilder.bind(jobAppliedQueue()).to(jobportalExchange()).with(JOB_APPLIED_KEY);
    }
    @Bean public Binding resumeUploadBinding() {
        return BindingBuilder.bind(resumeUploadQueue()).to(jobportalExchange()).with(RESUME_UPLOAD_KEY);
    }
    @Bean public Binding jobClosedBinding() {
        return BindingBuilder.bind(jobClosedQueue()).to(jobportalExchange()).with(JOB_CLOSED_KEY);
    }

    // ── DLQ Bindings ─────────────────────────────────────────────────────────

    @Bean public Binding jobCreatedDlqBinding() {
        return BindingBuilder.bind(jobCreatedDlq()).to(deadLetterExchange()).with(JOB_CREATED_QUEUE + ".dlq");
    }
    @Bean public Binding jobAppliedDlqBinding() {
        return BindingBuilder.bind(jobAppliedDlq()).to(deadLetterExchange()).with(JOB_APPLIED_QUEUE + ".dlq");
    }
    @Bean public Binding resumeUploadDlqBinding() {
        return BindingBuilder.bind(resumeUploadDlq()).to(deadLetterExchange()).with(RESUME_UPLOAD_QUEUE + ".dlq");
    }
    @Bean public Binding jobClosedDlqBinding() {
        return BindingBuilder.bind(jobClosedDlq()).to(deadLetterExchange()).with(JOB_CLOSED_QUEUE + ".dlq");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

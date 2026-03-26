package com.capg.analyticsservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "jobportal.exchange";

    // Analytics-specific queues (dedicated so fanout works correctly)
    public static final String JOB_CREATED_QUEUE  = "job.created.analytics.queue";
    public static final String JOB_APPLIED_QUEUE  = "job.applied.analytics.queue";
    public static final String RESUME_UPLOAD_QUEUE = "resume.upload.analytics.queue";
    public static final String JOB_CLOSED_QUEUE   = "job.closed.analytics.queue";

    public static final String JOB_CREATED_KEY  = "job.created";
    public static final String JOB_APPLIED_KEY  = "job.applied";
    public static final String RESUME_UPLOAD_KEY = "resume.uploaded";
    public static final String JOB_CLOSED_KEY   = "job.closed";

    @Bean public TopicExchange exchange() { return new TopicExchange(EXCHANGE); }

    @Bean public Queue jobCreatedQueue()   { return new Queue(JOB_CREATED_QUEUE); }
    @Bean public Queue jobAppliedQueue()   { return new Queue(JOB_APPLIED_QUEUE); }
    @Bean public Queue resumeUploadQueue() { return new Queue(RESUME_UPLOAD_QUEUE); }
    @Bean public Queue jobClosedQueue()    { return new Queue(JOB_CLOSED_QUEUE); }

    @Bean
    public Binding jobCreatedBinding() {
        return BindingBuilder.bind(jobCreatedQueue()).to(exchange()).with(JOB_CREATED_KEY);
    }
    @Bean
    public Binding jobAppliedBinding() {
        return BindingBuilder.bind(jobAppliedQueue()).to(exchange()).with(JOB_APPLIED_KEY);
    }
    @Bean
    public Binding resumeUploadBinding() {
        return BindingBuilder.bind(resumeUploadQueue()).to(exchange()).with(RESUME_UPLOAD_KEY);
    }
    @Bean
    public Binding jobClosedBinding() {
        return BindingBuilder.bind(jobClosedQueue()).to(exchange()).with(JOB_CLOSED_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
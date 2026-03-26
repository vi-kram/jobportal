package com.capg.notificationservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

@Configuration
public class RabbitMQConfig {

    public static final String JOB_CREATED_QUEUE   = "job.created.notify.queue";
    public static final String JOB_APPLIED_QUEUE   = "job.applied.notify.queue";
    public static final String JOB_CLOSED_QUEUE    = "job.closed.notify.queue";
    public static final String RESUME_UPLOAD_QUEUE = "resume.upload.notify.queue";

    @Bean public Queue jobCreatedQueue()   { return new Queue(JOB_CREATED_QUEUE); }
    @Bean public Queue jobAppliedQueue()   { return new Queue(JOB_APPLIED_QUEUE); }
    @Bean public Queue jobClosedQueue()    { return new Queue(JOB_CLOSED_QUEUE); }
    @Bean public Queue resumeUploadQueue() { return new Queue(RESUME_UPLOAD_QUEUE); }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("jobportal.exchange");
    }

    @Bean
    public Binding jobCreatedBinding() {
        return BindingBuilder.bind(jobCreatedQueue()).to(exchange()).with("job.created");
    }
    @Bean
    public Binding jobAppliedBinding() {
        return BindingBuilder.bind(jobAppliedQueue()).to(exchange()).with("job.applied");
    }
    @Bean
    public Binding jobClosedBinding() {
        return BindingBuilder.bind(jobClosedQueue()).to(exchange()).with("job.closed");
    }
    @Bean
    public Binding resumeBinding() {
        return BindingBuilder.bind(resumeUploadQueue()).to(exchange()).with("resume.uploaded");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
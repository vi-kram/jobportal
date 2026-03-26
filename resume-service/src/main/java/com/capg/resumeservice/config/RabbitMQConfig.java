package com.capg.resumeservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE    = "jobportal.exchange";
    public static final String RESUME_KEY  = "resume.uploaded";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // resume.uploaded — one queue per consumer service
    @Bean public Queue resumeAnalyticsQueue() { return new Queue("resume.upload.analytics.queue"); }
    @Bean public Queue resumeNotifyQueue()    { return new Queue("resume.upload.notify.queue"); }

    @Bean
    public Binding resumeAnalyticsBinding() {
        return BindingBuilder.bind(resumeAnalyticsQueue()).to(exchange()).with(RESUME_KEY);
    }
    @Bean
    public Binding resumeNotifyBinding() {
        return BindingBuilder.bind(resumeNotifyQueue()).to(exchange()).with(RESUME_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
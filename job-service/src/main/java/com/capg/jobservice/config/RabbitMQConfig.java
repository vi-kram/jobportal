package com.capg.jobservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE          = "jobportal.exchange";
    public static final String JOB_CREATED_KEY   = "job.created";
    public static final String JOB_CLOSED_KEY    = "job.closed";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // job.created — one queue per consumer service
    @Bean public Queue jobCreatedAnalyticsQueue() { return new Queue("job.created.analytics.queue"); }
    @Bean public Queue jobCreatedNotifyQueue()    { return new Queue("job.created.notify.queue"); }
    @Bean public Queue jobCreatedSearchQueue()    { return new Queue("job.created.search.queue"); }

    // job.closed — one queue per consumer service
    @Bean public Queue jobClosedAnalyticsQueue()  { return new Queue("job.closed.analytics.queue"); }
    @Bean public Queue jobClosedNotifyQueue()     { return new Queue("job.closed.notify.queue"); }

    // Bindings for job.created
    @Bean
    public Binding jobCreatedAnalyticsBinding() {
        return BindingBuilder.bind(jobCreatedAnalyticsQueue()).to(exchange()).with(JOB_CREATED_KEY);
    }
    @Bean
    public Binding jobCreatedNotifyBinding() {
        return BindingBuilder.bind(jobCreatedNotifyQueue()).to(exchange()).with(JOB_CREATED_KEY);
    }
    @Bean
    public Binding jobCreatedSearchBinding() {
        return BindingBuilder.bind(jobCreatedSearchQueue()).to(exchange()).with(JOB_CREATED_KEY);
    }

    // Bindings for job.closed
    @Bean
    public Binding jobClosedAnalyticsBinding() {
        return BindingBuilder.bind(jobClosedAnalyticsQueue()).to(exchange()).with(JOB_CLOSED_KEY);
    }
    @Bean
    public Binding jobClosedNotifyBinding() {
        return BindingBuilder.bind(jobClosedNotifyQueue()).to(exchange()).with(JOB_CLOSED_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
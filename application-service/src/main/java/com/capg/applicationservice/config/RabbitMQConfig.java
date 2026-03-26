package com.capg.applicationservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE         = "jobportal.exchange";
    public static final String JOB_APPLIED_KEY  = "job.applied";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    // job.applied — one queue per consumer service
    @Bean public Queue jobAppliedAnalyticsQueue() { return new Queue("job.applied.analytics.queue"); }
    @Bean public Queue jobAppliedNotifyQueue()    { return new Queue("job.applied.notify.queue"); }

    @Bean
    public Binding jobAppliedAnalyticsBinding() {
        return BindingBuilder.bind(jobAppliedAnalyticsQueue()).to(exchange()).with(JOB_APPLIED_KEY);
    }
    @Bean
    public Binding jobAppliedNotifyBinding() {
        return BindingBuilder.bind(jobAppliedNotifyQueue()).to(exchange()).with(JOB_APPLIED_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}

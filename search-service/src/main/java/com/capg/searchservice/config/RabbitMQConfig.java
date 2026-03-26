package com.capg.searchservice.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String JOB_QUEUE = "job.created.search.queue";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange("jobportal.exchange");
    }

    @Bean
    public Queue jobQueue() {
        return new Queue(JOB_QUEUE);
    }

    @Bean
    public Binding jobCreatedBinding() {
        return BindingBuilder.bind(jobQueue()).to(exchange()).with("job.created");
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
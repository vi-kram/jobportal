package com.capg.notificationservice.consumer;

import com.capg.notificationservice.config.RabbitMQConfig;
import com.capg.notificationservice.dto.ApplicationEvent;
import com.capg.notificationservice.dto.ResumeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @RabbitListener(queues = RabbitMQConfig.JOB_CREATED_QUEUE)
    public void handleJobCreated(Map<String, Object> message) {
        log.info("[Notification] Job created title={}", message.get("title"));
    }

    @RabbitListener(queues = RabbitMQConfig.JOB_APPLIED_QUEUE)
    public void handleJobApplied(Map<String, Object> message) {
        log.info("[Notification] Application received jobId={} candidate={}", message.get("jobId"), message.get("candidateId"));
    }

    @RabbitListener(queues = RabbitMQConfig.JOB_CLOSED_QUEUE)
    public void handleJobClosed(Map<String, Object> message) {
        log.info("[Notification] Job closed jobId={}", message.get("jobId"));
    }

    @RabbitListener(queues = RabbitMQConfig.RESUME_UPLOAD_QUEUE)
    public void handleResumeUploaded(ResumeEvent event) {
        log.info("[Notification] Resume uploaded userEmail={}", event.getUserEmail());
    }
}

//It listens to events from other microservices and sends notifications based on them.
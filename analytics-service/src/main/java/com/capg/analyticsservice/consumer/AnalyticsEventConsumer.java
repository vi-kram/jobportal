package com.capg.analyticsservice.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.capg.analyticsservice.service.AnalyticsService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AnalyticsEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsEventConsumer.class);

    private final AnalyticsService analyticsService;

    public AnalyticsEventConsumer(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @RabbitListener(queues = "job.created.analytics.queue")
    public void handleJobCreated(Map<String, Object> message) {
        log.info("Analytics event received type=JOB_CREATED");
        analyticsService.incrementMetric("JOB_CREATED");
    }

    @RabbitListener(queues = "job.applied.analytics.queue")
    public void handleJobApplied(Map<String, Object> message) {
        Long jobId = Long.valueOf(message.get("jobId").toString());
        String userEmail = message.get("candidateId").toString();
        log.info("Analytics event received type=JOB_APPLIED jobId={} email={}", jobId, userEmail);
        analyticsService.incrementMetric("JOB_APPLIED");
        analyticsService.incrementJobApplication(jobId);
        analyticsService.incrementUserApplication(userEmail);
    }

    @RabbitListener(queues = "resume.upload.analytics.queue")
    public void handleResumeUploaded(Map<String, Object> message) {
        log.info("Analytics event received type=RESUME_UPLOADED");
        analyticsService.incrementMetric("RESUME_UPLOADED");
    }

    @RabbitListener(queues = "job.closed.analytics.queue")
    public void handleJobClosed(Map<String, Object> message) {
        log.info("Analytics event received type=JOB_CLOSED");
        analyticsService.incrementMetric("JOB_CLOSED");
    }
}

//It listens to events from other microservices and updates analytics data in your database.
//Receive Event → Extract Data → Call Service → Update DB
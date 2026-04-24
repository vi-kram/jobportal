package com.capg.notificationservice.consumer;

import com.capg.notificationservice.config.RabbitMQConfig;
import com.capg.notificationservice.dto.ApplicationEvent;
import com.capg.notificationservice.dto.JobClosedEvent;
import com.capg.notificationservice.dto.JobEvent;
import com.capg.notificationservice.dto.ResumeEvent;
import com.capg.notificationservice.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private static final String SIGNATURE = "\n\nJob Portal Team";

    private final EmailService emailService;

    @Value("${spring.mail.username}")
    private String adminEmail;

    public NotificationConsumer(EmailService emailService) {
        this.emailService = emailService;
    }

    @RabbitListener(queues = RabbitMQConfig.JOB_CREATED_QUEUE)
    public void handleJobCreated(JobEvent event) {
        log.info("[Notification] Job created jobId={} title={}", event.getJobId(), event.getTitle());
        emailService.send(
            event.getCreatedBy(),
            "Job Posted Successfully - " + event.getTitle(),
            "Hi,\n\nYour job \"" + event.getTitle() + "\" at " + event.getCompany() +
            " in " + event.getLocation() + " has been posted successfully." +
            "\nSalary: " + event.getSalary() + SIGNATURE
        );
    }

    @RabbitListener(queues = RabbitMQConfig.JOB_APPLIED_QUEUE)
    public void handleJobApplied(ApplicationEvent event) {
        log.info("[Notification] Job applied jobId={} candidate={}", event.getJobId(), event.getUserEmail());
        emailService.send(
            event.getUserEmail(),
            "Application Received",
            "Hi,\n\nYour application for job ID \"" + event.getJobId() + "\" has been received." +
            "\n\nApplication ID: " + event.getApplicationId() +
            "\nStatus: " + event.getStatus() + SIGNATURE
        );
    }

    @RabbitListener(queues = RabbitMQConfig.JOB_CLOSED_QUEUE)
    public void handleJobClosed(JobClosedEvent event) {
        log.info("[Notification] Job closed jobId={} title={}", event.getJobId(), event.getTitle());
        emailService.send(
            event.getCreatedBy(),
            "Job Closed - " + event.getTitle(),
            "Hi,\n\nYour job posting \"" + event.getTitle() + "\" has been closed successfully." + SIGNATURE
        );
    }

    @RabbitListener(queues = RabbitMQConfig.RESUME_UPLOAD_QUEUE)
    public void handleResumeUploaded(ResumeEvent event) {
        log.info("[Notification] Resume uploaded userEmail={}", event.getUserEmail());
        emailService.send(
            event.getUserEmail(),
            "Resume Uploaded Successfully",
            "Hi,\n\nYour resume has been uploaded successfully." +
            "\n\nResume ID: " + event.getResumeId() + SIGNATURE
        );
    }
}

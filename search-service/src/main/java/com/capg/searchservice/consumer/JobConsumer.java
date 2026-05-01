package com.capg.searchservice.consumer;

import com.capg.searchservice.config.RabbitMQConfig;
import com.capg.searchservice.dto.JobClosedEvent;
import com.capg.searchservice.dto.JobEvent;
import com.capg.searchservice.entity.Job;
import com.capg.searchservice.repository.JobRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class JobConsumer {

    private static final Logger log = LoggerFactory.getLogger(JobConsumer.class);

    private final JobRepository repository;

    public JobConsumer(JobRepository repository) {
        this.repository = repository;
    }

    @RabbitListener(queues = RabbitMQConfig.JOB_CREATED_QUEUE)
    public void handleJobCreated(JobEvent event) {
        log.info("Job created event received jobId={} title={}", event.getJobId(), event.getTitle());

        Job job = new Job();
        job.setJobId(event.getJobId());
        job.setTitle(event.getTitle());
        job.setCompany(event.getCompany());
        job.setLocation(event.getLocation());
        job.setSalary(event.getSalary());
        job.setDescription(event.getDescription());
        job.setSkills(event.getSkills());
        job.setJobType(event.getJobType());
        job.setExperienceLevel(event.getExperienceLevel());
        job.setStatus("OPEN");

        repository.save(job);
        log.info("Job indexed for search jobId={} title={}", event.getJobId(), event.getTitle());
    }

    @RabbitListener(queues = RabbitMQConfig.JOB_CLOSED_QUEUE)
    public void handleJobClosed(JobClosedEvent event) {
        log.info("Job closed event received jobId={}", event.getJobId());

        repository.findById(event.getJobId()).ifPresentOrElse(job -> {
            job.setStatus("CLOSED");
            repository.save(job);
            log.info("Job marked as CLOSED in search index jobId={}", event.getJobId());
        }, () -> log.warn("Job not found in search index jobId={}", event.getJobId()));
    }
}

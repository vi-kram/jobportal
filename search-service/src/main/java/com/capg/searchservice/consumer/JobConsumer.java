package com.capg.searchservice.consumer;

import com.capg.searchservice.config.RabbitMQConfig;
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

    @RabbitListener(queues = RabbitMQConfig.JOB_QUEUE)
    public void consume(JobEvent event) {
        log.info("Job event received jobId={} title={}", event.getJobId(), event.getTitle());

        Job job = new Job();
        job.setTitle(event.getTitle());
        job.setCompany(event.getCompany());
        job.setLocation(event.getLocation());
        job.setSalary(event.getSalary());
        job.setDescription(event.getDescription());

        repository.save(job);
        log.info("Job indexed for search jobId={} title={}", event.getJobId(), event.getTitle());
    }
}  


//1. Job Service creates job
//2. Sends JobEvent → RabbitMQ (job.queue)
//3. Search Service listens
//4. consume() method triggered
//5. Convert JobEvent → Job
//6. Save into database
package com.capg.applicationservice.service.impl;

import com.capg.applicationservice.client.JobClient;
import com.capg.applicationservice.dto.ApplicationEvent;
import com.capg.applicationservice.dto.request.ApplicationRequest;
import com.capg.applicationservice.dto.response.ApplicationResponse;
import com.capg.applicationservice.entity.Application;
import com.capg.applicationservice.entity.ApplicationStatus;
import com.capg.applicationservice.exception.AlreadyAppliedException;
import com.capg.applicationservice.exception.AlreadyRejectedException;
import com.capg.applicationservice.exception.InvalidStatusException;
import com.capg.applicationservice.exception.ResourceNotFoundException;
import com.capg.applicationservice.exception.UnauthorizedException;
import com.capg.applicationservice.repository.ApplicationRepository;
import com.capg.applicationservice.service.ApplicationService;

import com.capg.applicationservice.mapper.ApplicationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicationServiceImpl.class);

    private final ApplicationRepository repository;
    private final JobClient jobClient;
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationMapper applicationMapper;

    public ApplicationServiceImpl(ApplicationRepository repository,
                                  JobClient jobClient,
                                  RabbitTemplate rabbitTemplate,
                                  ApplicationMapper applicationMapper) {
        this.repository = repository;
        this.jobClient = jobClient;
        this.rabbitTemplate = rabbitTemplate;
        this.applicationMapper = applicationMapper;
    }

    @Override
    @Transactional
    public ApplicationResponse apply(ApplicationRequest request, String email, String role) {

        log.info("Apply request jobId={} applicant={}", request.getJobId(), email);

        if (!role.equals("JOB_SEEKER")) {
            log.warn("Apply rejected - not a job seeker email={} role={}", email, role);
            throw new UnauthorizedException("Only job seekers can apply");
        }

        if (repository.existsByJobIdAndUserEmail(request.getJobId(), email)) {
            log.warn("Duplicate application jobId={} email={}", request.getJobId(), email);
            throw new AlreadyAppliedException("Already applied to this job");
        }

        //  Create application
        Application app = new Application();
        app.setJobId(request.getJobId());
        app.setUserEmail(email);
        app.setStatus(ApplicationStatus.APPLIED);
        app.setAppliedAt(LocalDateTime.now());

        Application saved = repository.save(app);
        log.info("Application saved applicationId={} jobId={} email={}", saved.getApplicationId(), saved.getJobId(), email);

        try {
            ApplicationEvent event = new ApplicationEvent(
                    saved.getApplicationId().toString(),
                    saved.getJobId(),
                    saved.getUserEmail(),
                    saved.getStatus().name()
            );
            rabbitTemplate.convertAndSend("jobportal.exchange", "job.applied", event);
            log.info("RabbitMQ event published exchange=jobportal.exchange routingKey=job.applied jobId={}", saved.getJobId());

        } catch (Exception e) {
            log.error("RabbitMQ publish failed applicationId={}", saved.getApplicationId(), e);
        }

        return map(saved);
    }

    @Override
    public Page<ApplicationResponse> getMyApplications(String email, int page, int size) {
        return repository.findByUserEmail(email, PageRequest.of(page, size))
                .map(this::map);
    }

    @Override
    public Page<ApplicationResponse> getApplicants(Long jobId, String role, int page, int size) {

        if (!role.equals("RECRUITER")) {
            log.warn("Unauthorized applicants view role={}", role);
            throw new UnauthorizedException("Only recruiters can view applicants");
        }

        log.info("Fetching applicants jobId={}", jobId);

        return repository.findByJobId(jobId, PageRequest.of(page, size))
                .map(this::map);
    }

    @Override
    @Transactional
    public ApplicationResponse updateStatus(UUID applicationId, String status, String role) {

        if (!role.equals("RECRUITER")) {
            log.warn("Unauthorized status update role={}", role);
            throw new UnauthorizedException("Only recruiters can update application status");
        }

        Application app = repository.findById(applicationId)
                .orElseThrow(() -> {
                    log.warn("Application not found applicationId={}", applicationId);
                    return new ResourceNotFoundException("Application not found");
                });

        ApplicationStatus newStatus;
        try {
            newStatus = ApplicationStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            log.warn("Invalid status value={}", status);
            throw new InvalidStatusException("Invalid status. Allowed: APPLIED, SHORTLISTED, INTERVIEW_SCHEDULED, REJECTED");
        }

        if (app.getStatus() == ApplicationStatus.REJECTED) {
            log.warn("Cannot update rejected application applicationId={}", applicationId);
            throw new AlreadyRejectedException("Cannot update a rejected application");
        }

        app.setStatus(newStatus);
        Application updated = repository.save(app);
        log.info("Application status updated applicationId={} newStatus={}", applicationId, newStatus);
        return map(updated);
    }

    private ApplicationResponse map(Application app) {
        return applicationMapper.toResponse(app);
    }

    @Override
    @Transactional
    public void withdrawApplication(UUID applicationId, String email) {
        Application app = repository.findById(applicationId)
                .orElseThrow(() -> {
                    log.warn("Application not found applicationId={}", applicationId);
                    return new ResourceNotFoundException("Application not found");
                });
        if (!app.getUserEmail().equals(email)) {
            log.warn("Unauthorized withdraw attempt applicationId={} email={}", applicationId, email);
            throw new UnauthorizedException("You can only withdraw your own applications");
        }
        if (app.getStatus() != ApplicationStatus.APPLIED) {
            throw new InvalidStatusException("Only APPLIED applications can be withdrawn");
        }
        repository.delete(app);
        log.info("Application withdrawn applicationId={} email={}", applicationId, email);
    }
}


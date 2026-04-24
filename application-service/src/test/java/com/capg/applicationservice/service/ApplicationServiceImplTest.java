package com.capg.applicationservice.service;

import com.capg.applicationservice.client.JobClient;
import com.capg.applicationservice.dto.request.ApplicationRequest;
import com.capg.applicationservice.dto.response.ApplicationResponse;
import com.capg.applicationservice.entity.Application;
import com.capg.applicationservice.entity.ApplicationStatus;
import com.capg.applicationservice.exception.AlreadyAppliedException;
import com.capg.applicationservice.exception.AlreadyRejectedException;
import com.capg.applicationservice.exception.ResourceNotFoundException;
import com.capg.applicationservice.exception.UnauthorizedException;
import com.capg.applicationservice.mapper.ApplicationMapper;
import com.capg.applicationservice.repository.ApplicationRepository;
import com.capg.applicationservice.service.impl.ApplicationServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApplicationServiceImplTest {

    @Mock private ApplicationRepository repository;
    @Mock private JobClient jobClient;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private ApplicationMapper applicationMapper;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    @Test
    void apply_success() {
        ApplicationRequest request = new ApplicationRequest(1L);
        UUID appId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Application saved = new Application(appId, 1L, "seeker@test.com", ApplicationStatus.APPLIED, now);
        ApplicationResponse expectedResponse = new ApplicationResponse(appId, 1L, "seeker@test.com", ApplicationStatus.APPLIED, now);

        when(jobClient.getJobById(1L)).thenReturn(new Object());
        when(repository.existsByJobIdAndUserEmail(1L, "seeker@test.com")).thenReturn(false);
        when(repository.save(any(Application.class))).thenReturn(saved);
        when(applicationMapper.toResponse(saved)).thenReturn(expectedResponse);

        ApplicationResponse response = applicationService.apply(request, "seeker@test.com", "JOB_SEEKER");

        assertNotNull(response);
        assertEquals(appId, response.getApplicationId());
        assertEquals(1L, response.getJobId());
        assertEquals(ApplicationStatus.APPLIED, response.getStatus());
        verify(repository).save(any(Application.class));
    }

    @Test
    void apply_notJobSeeker_throwsException() {
        ApplicationRequest request = new ApplicationRequest(1L);

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> applicationService.apply(request, "recruiter@test.com", "RECRUITER"));

        assertEquals("Only job seekers can apply", ex.getMessage());
        verify(repository, never()).save(any(Application.class));
    }

    @Test
    void apply_duplicateApplication_throwsException() {
        ApplicationRequest request = new ApplicationRequest(1L);

        when(jobClient.getJobById(1L)).thenReturn(new Object());
        when(repository.existsByJobIdAndUserEmail(1L, "seeker@test.com")).thenReturn(true);

        AlreadyAppliedException ex = assertThrows(AlreadyAppliedException.class,
                () -> applicationService.apply(request, "seeker@test.com", "JOB_SEEKER"));

        assertEquals("Already applied to this job", ex.getMessage());
        verify(repository, never()).save(any(Application.class));
    }

    @Test
    void getApplicants_success() {
        LocalDateTime now = LocalDateTime.now();
        UUID appId = UUID.randomUUID();
        Application app = new Application(appId, 1L, "seeker@test.com", ApplicationStatus.APPLIED, now);
        ApplicationResponse mappedResponse = new ApplicationResponse(appId, 1L, "seeker@test.com", ApplicationStatus.APPLIED, now);

        Page<Application> page = new PageImpl<>(List.of(app), PageRequest.of(0, 10), 1);
        when(repository.findByJobId(1L, PageRequest.of(0, 10))).thenReturn(page);
        when(applicationMapper.toResponse(app)).thenReturn(mappedResponse);

        Page<ApplicationResponse> result = applicationService.getApplicants(1L, "RECRUITER", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getApplicants_notRecruiter_throwsException() {
        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> applicationService.getApplicants(1L, "JOB_SEEKER", 0, 10));

        assertEquals("Only recruiters can view applicants", ex.getMessage());
        verify(repository, never()).findByJobId(any(), any());
    }

    @Test
    void updateStatus_success() {
        UUID appId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Application app = new Application(appId, 1L, "seeker@test.com", ApplicationStatus.APPLIED, now);
        Application updated = new Application(appId, 1L, "seeker@test.com", ApplicationStatus.SHORTLISTED, now);
        ApplicationResponse mappedResponse = new ApplicationResponse(appId, 1L, "seeker@test.com", ApplicationStatus.SHORTLISTED, now);

        when(repository.findById(appId)).thenReturn(Optional.of(app));
        when(repository.save(any(Application.class))).thenReturn(updated);
        when(applicationMapper.toResponse(updated)).thenReturn(mappedResponse);

        ApplicationResponse response = applicationService.updateStatus(appId, "SHORTLISTED", "RECRUITER");

        assertNotNull(response);
        assertEquals(ApplicationStatus.SHORTLISTED, response.getStatus());
        verify(repository).save(any(Application.class));
    }

    @Test
    void updateStatus_notRecruiter_throwsException() {
        UUID appId = UUID.randomUUID();

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> applicationService.updateStatus(appId, "SHORTLISTED", "JOB_SEEKER"));

        assertEquals("Only recruiters can update application status", ex.getMessage());
        verify(repository, never()).save(any(Application.class));
    }

    @Test
    void updateStatus_applicationNotFound_throwsException() {
        UUID appId = UUID.randomUUID();
        when(repository.findById(appId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> applicationService.updateStatus(appId, "SHORTLISTED", "RECRUITER"));

        assertEquals("Application not found", ex.getMessage());
        verify(repository, never()).save(any(Application.class));
    }

    @Test
    void updateStatus_rejectedApplication_throwsException() {
        UUID appId = UUID.randomUUID();
        Application app = new Application(appId, 1L, "seeker@test.com", ApplicationStatus.REJECTED, LocalDateTime.now());

        when(repository.findById(appId)).thenReturn(Optional.of(app));

        AlreadyRejectedException ex = assertThrows(AlreadyRejectedException.class,
                () -> applicationService.updateStatus(appId, "SHORTLISTED", "RECRUITER"));

        assertEquals("Cannot update a rejected application", ex.getMessage());
        verify(repository, never()).save(any(Application.class));
    }

    @Test
    void getMyApplications_returnsPage() {
        LocalDateTime now = LocalDateTime.now();
        UUID appId = UUID.randomUUID();
        Application app = new Application(appId, 1L, "seeker@test.com", ApplicationStatus.APPLIED, now);
        ApplicationResponse mappedResponse = new ApplicationResponse(appId, 1L, "seeker@test.com", ApplicationStatus.APPLIED, now);

        Page<Application> page = new PageImpl<>(List.of(app), PageRequest.of(0, 10), 1);
        when(repository.findByUserEmail("seeker@test.com", PageRequest.of(0, 10))).thenReturn(page);
        when(applicationMapper.toResponse(app)).thenReturn(mappedResponse);

        Page<ApplicationResponse> result = applicationService.getMyApplications("seeker@test.com", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void updateStatus_invalidStatus_throwsException() {
        UUID appId = UUID.randomUUID();
        Application app = new Application(appId, 1L, "seeker@test.com", ApplicationStatus.APPLIED, LocalDateTime.now());
        when(repository.findById(appId)).thenReturn(Optional.of(app));

        assertThrows(com.capg.applicationservice.exception.InvalidStatusException.class,
                () -> applicationService.updateStatus(appId, "INVALID_STATUS", "RECRUITER"));
    }

    @Test
    void apply_rabbitMqFailure_stillReturnsResponse() {
        ApplicationRequest request = new ApplicationRequest(1L);
        UUID appId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Application saved = new Application(appId, 1L, "seeker@test.com", ApplicationStatus.APPLIED, now);
        ApplicationResponse expectedResponse = new ApplicationResponse(appId, 1L, "seeker@test.com", ApplicationStatus.APPLIED, now);

        when(jobClient.getJobById(1L)).thenReturn(new Object());
        when(repository.existsByJobIdAndUserEmail(1L, "seeker@test.com")).thenReturn(false);
        when(repository.save(any(Application.class))).thenReturn(saved);
        when(applicationMapper.toResponse(saved)).thenReturn(expectedResponse);
        doThrow(new RuntimeException("RabbitMQ down"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        ApplicationResponse response = applicationService.apply(request, "seeker@test.com", "JOB_SEEKER");

        assertNotNull(response);
        assertEquals(appId, response.getApplicationId());
    }
}

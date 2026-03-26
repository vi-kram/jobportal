package com.capg.applicationservice.service;

import com.capg.applicationservice.client.JobClient;
import com.capg.applicationservice.dto.request.ApplicationRequest;
import com.capg.applicationservice.dto.response.ApplicationResponse;
import com.capg.applicationservice.entity.Application;
import com.capg.applicationservice.entity.ApplicationStatus;
import com.capg.applicationservice.exception.AlreadyAppliedException;
import com.capg.applicationservice.exception.ResourceNotFoundException;
import com.capg.applicationservice.exception.UnauthorizedException;
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

    @Mock
    private ApplicationRepository repository;

    @Mock
    private JobClient jobClient;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ApplicationServiceImpl applicationService;

    // =============================================
    // apply tests
    // =============================================

    @Test
    void apply_success() {
        // Arrange
        ApplicationRequest request = new ApplicationRequest(1L);
        UUID appId = UUID.randomUUID();

        Application saved = new Application(
                appId, 1L, "seeker@test.com",
                ApplicationStatus.APPLIED, LocalDateTime.now()
        );

        when(jobClient.getJobById(1L)).thenReturn(new Object());
        when(repository.existsByJobIdAndUserEmail(1L, "seeker@test.com")).thenReturn(false);
        when(repository.save(any(Application.class))).thenReturn(saved);

        // Act
        ApplicationResponse response = applicationService.apply(request, "seeker@test.com", "JOB_SEEKER");

        // Assert
        assertNotNull(response);
        assertEquals(appId, response.getApplicationId());
        assertEquals(1L, response.getJobId());
        assertEquals(ApplicationStatus.APPLIED, response.getStatus());

        verify(jobClient).getJobById(1L);
        verify(repository).existsByJobIdAndUserEmail(1L, "seeker@test.com");
        verify(repository).save(any(Application.class));
    }

    @Test
    void apply_notJobSeeker_throwsException() {
        // Arrange
        ApplicationRequest request = new ApplicationRequest(1L);

        // Act & Assert
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> applicationService.apply(request, "recruiter@test.com", "RECRUITER")
        );

        assertEquals("Only job seekers can apply", ex.getMessage());
        verify(repository, never()).save(any(Application.class));
    }

    @Test
    void apply_duplicateApplication_throwsException() {
        // Arrange
        ApplicationRequest request = new ApplicationRequest(1L);

        when(jobClient.getJobById(1L)).thenReturn(new Object());
        when(repository.existsByJobIdAndUserEmail(1L, "seeker@test.com")).thenReturn(true);

        // Act & Assert
        AlreadyAppliedException ex = assertThrows(
                AlreadyAppliedException.class,
                () -> applicationService.apply(request, "seeker@test.com", "JOB_SEEKER")
        );

        assertEquals("Already applied to this job", ex.getMessage());
        verify(repository, never()).save(any(Application.class));
    }

    // =============================================
    // getApplicants tests
    // =============================================

    @Test
    void getApplicants_success() {
        // Arrange
        Application app = new Application(
                UUID.randomUUID(), 1L, "seeker@test.com",
                ApplicationStatus.APPLIED, LocalDateTime.now()
        );

        Page<Application> page = new PageImpl<>(List.of(app), PageRequest.of(0, 10), 1);
        when(repository.findByJobId(1L, PageRequest.of(0, 10))).thenReturn(page);

        // Act
        Page<ApplicationResponse> result = applicationService.getApplicants(1L, "RECRUITER", 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(repository).findByJobId(1L, PageRequest.of(0, 10));
    }

    @Test
    void getApplicants_notRecruiter_throwsException() {
        // Act & Assert
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> applicationService.getApplicants(1L, "JOB_SEEKER", 0, 10)
        );

        assertEquals("Only recruiters can view applicants", ex.getMessage());
        verify(repository, never()).findByJobId(any(), any());
    }

    // =============================================
    // updateStatus tests
    // =============================================

    @Test
    void updateStatus_success() {
        // Arrange
        UUID appId = UUID.randomUUID();
        Application app = new Application(
                appId, 1L, "seeker@test.com",
                ApplicationStatus.APPLIED, LocalDateTime.now()
        );

        Application updated = new Application(
                appId, 1L, "seeker@test.com",
                ApplicationStatus.SHORTLISTED, LocalDateTime.now()
        );

        when(repository.findById(appId)).thenReturn(Optional.of(app));
        when(repository.save(any(Application.class))).thenReturn(updated);

        // Act
        ApplicationResponse response = applicationService.updateStatus(appId, "SHORTLISTED", "RECRUITER");

        // Assert
        assertNotNull(response);
        assertEquals(ApplicationStatus.SHORTLISTED, response.getStatus());

        verify(repository).findById(appId);
        verify(repository).save(any(Application.class));
    }

    @Test
    void updateStatus_notRecruiter_throwsException() {
        // Arrange
        UUID appId = UUID.randomUUID();

        // Act & Assert
        UnauthorizedException ex = assertThrows(
                UnauthorizedException.class,
                () -> applicationService.updateStatus(appId, "SHORTLISTED", "JOB_SEEKER")
        );

        assertEquals("Only recruiters can update application status", ex.getMessage());
        verify(repository, never()).save(any(Application.class));
    }

    @Test
    void updateStatus_applicationNotFound_throwsException() {
        // Arrange
        UUID appId = UUID.randomUUID();
        when(repository.findById(appId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> applicationService.updateStatus(appId, "SHORTLISTED", "RECRUITER")
        );

        assertEquals("Application not found", ex.getMessage());
        verify(repository, never()).save(any(Application.class));
    }

    @Test
    void updateStatus_rejectedApplication_throwsException() {
        // Arrange
        UUID appId = UUID.randomUUID();
        Application app = new Application(
                appId, 1L, "seeker@test.com",
                ApplicationStatus.REJECTED, LocalDateTime.now()
        );

        when(repository.findById(appId)).thenReturn(Optional.of(app));

        // Act & Assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> applicationService.updateStatus(appId, "SHORTLISTED", "RECRUITER")
        );

        assertEquals("Cannot update a rejected application", ex.getMessage());
        verify(repository, never()).save(any(Application.class));
    }
}

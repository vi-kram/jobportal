package com.capg.jobservice.service;

import com.capg.jobservice.dto.request.JobRequest;
import com.capg.jobservice.dto.response.JobResponse;
import com.capg.jobservice.entity.Job;
import com.capg.jobservice.exception.JobNotFoundException;
import com.capg.jobservice.repository.JobRepository;
import com.capg.jobservice.service.impl.JobServiceImpl;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceImplTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private JobServiceImpl jobService;

    // =============================================
    // createJob tests
    // =============================================

    @Test
    void createJob_success() {
        // Arrange
        JobRequest request = new JobRequest();
        request.setTitle("Java Developer");
        request.setCompany("Tech Corp");
        request.setLocation("Bangalore");
        request.setSalary(1200000.0);
        request.setDescription("Spring Boot developer");

        Job savedJob = new Job(
                1L, "Java Developer", "Tech Corp", "Bangalore",
                1200000.0, "Spring Boot developer", "OPEN",
                "recruiter@test.com", LocalDateTime.now(), LocalDateTime.now()
        );

        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);

        // Act
        JobResponse response = jobService.createJob(request, "recruiter@test.com");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getJobId());
        assertEquals("Java Developer", response.getTitle());
        assertEquals("OPEN", response.getStatus());
        assertEquals("recruiter@test.com", response.getCreatedBy());

        verify(jobRepository).save(any(Job.class));
    }

    @Test
    void createJob_rabbitMqFails_stillReturnsResponse() {
        // Arrange
        JobRequest request = new JobRequest();
        request.setTitle("Java Developer");
        request.setCompany("Tech Corp");
        request.setLocation("Bangalore");

        Job savedJob = new Job(
                1L, "Java Developer", "Tech Corp", "Bangalore",
                null, null, "OPEN",
                "recruiter@test.com", LocalDateTime.now(), LocalDateTime.now()
        );

        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);
        doThrow(new RuntimeException("RabbitMQ down"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        // Act — should NOT throw even when RabbitMQ fails
        JobResponse response = jobService.createJob(request, "recruiter@test.com");

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getJobId());
        verify(jobRepository).save(any(Job.class));
    }

    // =============================================
    // getJobById tests
    // =============================================

    @Test
    void getJobById_success() {
        // Arrange
        Job job = new Job(
                1L, "Java Developer", "Tech Corp", "Bangalore",
                1200000.0, "Spring Boot developer", "OPEN",
                "recruiter@test.com", LocalDateTime.now(), LocalDateTime.now()
        );

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        // Act
        JobResponse response = jobService.getJobById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getJobId());
        assertEquals("Java Developer", response.getTitle());
        assertEquals("OPEN", response.getStatus());

        verify(jobRepository).findById(1L);
    }

    @Test
    void getJobById_notFound_throwsException() {
        // Arrange
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        JobNotFoundException ex = assertThrows(
                JobNotFoundException.class,
                () -> jobService.getJobById(99L)
        );

        assertEquals("Job not found", ex.getMessage());
        verify(jobRepository).findById(99L);
    }

    // =============================================
    // getAllJobs tests
    // =============================================

    @Test
    void getAllJobs_returnsPage() {
        // Arrange
        Job job = new Job(
                1L, "Java Developer", "Tech Corp", "Bangalore",
                1200000.0, "Spring Boot developer", "OPEN",
                "recruiter@test.com", LocalDateTime.now(), LocalDateTime.now()
        );

        Page<Job> jobPage = new PageImpl<>(List.of(job), PageRequest.of(0, 10), 1);
        when(jobRepository.findAll(any(PageRequest.class))).thenReturn(jobPage);

        // Act
        Page<JobResponse> result = jobService.getAllJobs(0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Java Developer", result.getContent().get(0).getTitle());

        verify(jobRepository).findAll(any(PageRequest.class));
    }

    // =============================================
    // closeJob tests
    // =============================================

    @Test
    void closeJob_success() {
        // Arrange
        Job job = new Job(
                1L, "Java Developer", "Tech Corp", "Bangalore",
                1200000.0, "Spring Boot developer", "OPEN",
                "recruiter@test.com", LocalDateTime.now(), LocalDateTime.now()
        );

        Job closedJob = new Job(
                1L, "Java Developer", "Tech Corp", "Bangalore",
                1200000.0, "Spring Boot developer", "CLOSED",
                "recruiter@test.com", LocalDateTime.now(), LocalDateTime.now()
        );

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenReturn(closedJob);

        // Act
        Job result = jobService.closeJob(1L);

        // Assert
        assertNotNull(result);
        assertEquals("CLOSED", result.getStatus());

        verify(jobRepository).findById(1L);
        verify(jobRepository).save(any(Job.class));
    }

    @Test
    void closeJob_notFound_throwsException() {
        // Arrange
        when(jobRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        JobNotFoundException ex = assertThrows(
                JobNotFoundException.class,
                () -> jobService.closeJob(99L)
        );

        assertEquals("Job not found", ex.getMessage());
        verify(jobRepository).findById(99L);
        verify(jobRepository, never()).save(any(Job.class));
    }
}

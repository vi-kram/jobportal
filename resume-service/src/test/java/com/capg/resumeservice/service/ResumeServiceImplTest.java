package com.capg.resumeservice.service;

import com.capg.resumeservice.dto.request.ResumeUploadRequest;
import com.capg.resumeservice.dto.response.ResumeResponse;
import com.capg.resumeservice.entity.Resume;
import com.capg.resumeservice.repository.ResumeRepository;
import com.capg.resumeservice.service.impl.ResumeServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceImplTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    // =============================================
    // uploadResume tests
    // =============================================

    @Test
    void uploadResume_success() {
        // Arrange
        ResumeUploadRequest request = new ResumeUploadRequest(
                1L, "s3://bucket/resume.pdf", "seeker@test.com"
        );

        Resume saved = new Resume(
                1L, 1L, "s3://bucket/resume.pdf", LocalDateTime.now()
        );

        when(resumeRepository.save(any(Resume.class))).thenReturn(saved);

        // Act
        ResumeResponse response = resumeService.uploadResume(request);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getResumeId());
        assertEquals(1L, response.getUserId());
        assertEquals("s3://bucket/resume.pdf", response.getFileUrl());

        verify(resumeRepository).save(any(Resume.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void uploadResume_publishesRabbitMQEvent() {
        // Arrange
        ResumeUploadRequest request = new ResumeUploadRequest(
                1L, "s3://bucket/resume.pdf", "seeker@test.com"
        );

        Resume saved = new Resume(1L, 1L, "s3://bucket/resume.pdf", LocalDateTime.now());
        when(resumeRepository.save(any(Resume.class))).thenReturn(saved);

        // Act
        resumeService.uploadResume(request);

        // Assert — verify RabbitMQ was called exactly once with correct exchange
        verify(rabbitTemplate, times(1))
                .convertAndSend(eq("jobportal.exchange"), eq("resume.uploaded"), any(Object.class));
    }

    // =============================================
    // getResumeById tests
    // =============================================

    @Test
    void getResumeById_success() {
        // Arrange
        Resume resume = new Resume(1L, 1L, "s3://bucket/resume.pdf", LocalDateTime.now());
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));

        // Act
        ResumeResponse response = resumeService.getResumeById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getResumeId());
        assertEquals("s3://bucket/resume.pdf", response.getFileUrl());

        verify(resumeRepository).findById(1L);
    }

    @Test
    void getResumeById_notFound_throwsException() {
        // Arrange
        when(resumeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> resumeService.getResumeById(99L)
        );

        assertEquals("Resume not found", ex.getMessage());
        verify(resumeRepository).findById(99L);
    }

    // =============================================
    // getResumesByUserId tests
    // =============================================

    @Test
    void getResumesByUserId_returnsAll() {
        // Arrange
        List<Resume> resumes = List.of(
                new Resume(1L, 1L, "s3://bucket/resume1.pdf", LocalDateTime.now()),
                new Resume(2L, 1L, "s3://bucket/resume2.pdf", LocalDateTime.now())
        );

        when(resumeRepository.findByUserId(1L)).thenReturn(resumes);

        // Act
        List<ResumeResponse> result = resumeService.getResumesByUserId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("s3://bucket/resume1.pdf", result.get(0).getFileUrl());
        assertEquals("s3://bucket/resume2.pdf", result.get(1).getFileUrl());

        verify(resumeRepository).findByUserId(1L);
    }

    @Test
    void getResumesByUserId_noResumes_returnsEmptyList() {
        // Arrange
        when(resumeRepository.findByUserId(99L)).thenReturn(List.of());

        // Act
        List<ResumeResponse> result = resumeService.getResumesByUserId(99L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(resumeRepository).findByUserId(99L);
    }
}

package com.capg.resumeservice.service;

import com.capg.resumeservice.dto.request.ResumeUploadRequest;
import com.capg.resumeservice.dto.response.ResumeResponse;
import com.capg.resumeservice.entity.Resume;
import com.capg.resumeservice.exception.ResumeNotFoundException;
import com.capg.resumeservice.exception.UnauthorizedException;
import com.capg.resumeservice.mapper.ResumeMapper;
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

    @Mock private ResumeRepository resumeRepository;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private ResumeMapper resumeMapper;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    private Resume buildResume(Long id, String email, String url) {
        Resume r = new Resume();
        r.setResumeId(id);
        r.setUserEmail(email);
        r.setFileUrl(url);
        r.setUploadedAt(LocalDateTime.now());
        return r;
    }

    private ResumeResponse buildResponse(Long id, String email, String url) {
        ResumeResponse res = new ResumeResponse();
        res.setResumeId(id);
        res.setUserEmail(email);
        res.setFileUrl(url);
        return res;
    }

    // uploadResume tests

    @Test
    void uploadResume_success() {
        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFileUrl("https://bucket/resume.pdf");

        Resume saved = buildResume(1L, "seeker@test.com", "https://bucket/resume.pdf");
        ResumeResponse expected = buildResponse(1L, "seeker@test.com", "https://bucket/resume.pdf");

        when(resumeRepository.save(any(Resume.class))).thenReturn(saved);
        when(resumeMapper.toResponse(saved)).thenReturn(expected);

        ResumeResponse response = resumeService.uploadResume(request, "seeker@test.com", "JOB_SEEKER");

        assertNotNull(response);
        assertEquals(1L, response.getResumeId());
        assertEquals("seeker@test.com", response.getUserEmail());
        assertEquals("https://bucket/resume.pdf", response.getFileUrl());

        verify(resumeRepository).save(any(Resume.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void uploadResume_unauthorized_throwsException() {
        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFileUrl("https://bucket/resume.pdf");

        assertThrows(UnauthorizedException.class,
                () -> resumeService.uploadResume(request, "recruiter@test.com", "RECRUITER"));

        verify(resumeRepository, never()).save(any());
    }

    @Test
    void uploadResume_rabbitMqFails_stillReturnsResponse() {
        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFileUrl("https://bucket/resume.pdf");

        Resume saved = buildResume(1L, "seeker@test.com", "https://bucket/resume.pdf");
        ResumeResponse expected = buildResponse(1L, "seeker@test.com", "https://bucket/resume.pdf");

        when(resumeRepository.save(any(Resume.class))).thenReturn(saved);
        when(resumeMapper.toResponse(saved)).thenReturn(expected);
        doThrow(new RuntimeException("RabbitMQ down"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        ResumeResponse response = resumeService.uploadResume(request, "seeker@test.com", "JOB_SEEKER");

        assertNotNull(response);
        verify(resumeRepository).save(any(Resume.class));
    }

    // getResumeById tests

    @Test
    void getResumeById_recruiter_success() {
        Resume resume = buildResume(1L, "seeker@test.com", "https://bucket/resume.pdf");
        ResumeResponse expected = buildResponse(1L, "seeker@test.com", "https://bucket/resume.pdf");

        when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));
        when(resumeMapper.toResponse(resume)).thenReturn(expected);

        ResumeResponse response = resumeService.getResumeById(1L, "recruiter@test.com", "RECRUITER");

        assertNotNull(response);
        assertEquals(1L, response.getResumeId());
        verify(resumeRepository).findById(1L);
    }

    @Test
    void getResumeById_jobSeeker_ownResume_success() {
        Resume resume = buildResume(1L, "seeker@test.com", "https://bucket/resume.pdf");
        ResumeResponse expected = buildResponse(1L, "seeker@test.com", "https://bucket/resume.pdf");

        when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));
        when(resumeMapper.toResponse(resume)).thenReturn(expected);

        ResumeResponse response = resumeService.getResumeById(1L, "seeker@test.com", "JOB_SEEKER");

        assertNotNull(response);
        verify(resumeRepository).findById(1L);
    }

    @Test
    void getResumeById_jobSeeker_otherResume_throwsUnauthorized() {
        Resume resume = buildResume(1L, "other@test.com", "https://bucket/resume.pdf");

        when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));

        assertThrows(UnauthorizedException.class,
                () -> resumeService.getResumeById(1L, "seeker@test.com", "JOB_SEEKER"));
    }

    @Test
    void getResumeById_notFound_throwsException() {
        when(resumeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResumeNotFoundException.class,
                () -> resumeService.getResumeById(99L, "seeker@test.com", "JOB_SEEKER"));

        verify(resumeRepository).findById(99L);
    }

    // getMyResumes tests

    @Test
    void getMyResumes_returnsAll() {
        List<Resume> resumes = List.of(
                buildResume(1L, "seeker@test.com", "https://bucket/resume1.pdf"),
                buildResume(2L, "seeker@test.com", "https://bucket/resume2.pdf")
        );

        when(resumeRepository.findByUserEmail("seeker@test.com")).thenReturn(resumes);
        when(resumeMapper.toResponse(any(Resume.class)))
                .thenReturn(buildResponse(1L, "seeker@test.com", "https://bucket/resume1.pdf"))
                .thenReturn(buildResponse(2L, "seeker@test.com", "https://bucket/resume2.pdf"));

        List<ResumeResponse> result = resumeService.getMyResumes("seeker@test.com");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(resumeRepository).findByUserEmail("seeker@test.com");
    }

    @Test
    void getMyResumes_noResumes_returnsEmptyList() {
        when(resumeRepository.findByUserEmail("seeker@test.com")).thenReturn(List.of());

        List<ResumeResponse> result = resumeService.getMyResumes("seeker@test.com");

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(resumeRepository).findByUserEmail("seeker@test.com");
    }

    // deleteResume tests

    @Test
    void deleteResume_success() {
        Resume resume = buildResume(1L, "seeker@test.com", "https://bucket/resume.pdf");
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));

        resumeService.deleteResume(1L, "seeker@test.com");

        verify(resumeRepository).delete(resume);
    }

    @Test
    void deleteResume_notOwner_throwsUnauthorized() {
        Resume resume = buildResume(1L, "other@test.com", "https://bucket/resume.pdf");
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));

        assertThrows(UnauthorizedException.class,
                () -> resumeService.deleteResume(1L, "seeker@test.com"));

        verify(resumeRepository, never()).delete(any());
    }

    @Test
    void deleteResume_notFound_throwsException() {
        when(resumeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResumeNotFoundException.class,
                () -> resumeService.deleteResume(99L, "seeker@test.com"));

        verify(resumeRepository, never()).delete(any());
    }
}

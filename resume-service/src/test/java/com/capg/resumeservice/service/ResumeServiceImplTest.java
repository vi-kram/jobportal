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
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceImplTest {

    @Mock private ResumeRepository resumeRepository;
    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private ResumeMapper resumeMapper;

    @InjectMocks
    private ResumeServiceImpl resumeService;

    private Resume buildResume(Long id, String email) {
        Resume r = new Resume();
        r.setResumeId(id);
        r.setUserEmail(email);
        r.setFileUrl("/uploads/resumes/test.pdf");
        r.setUploadedAt(LocalDateTime.now());
        return r;
    }

    private ResumeResponse buildResponse(Long id, String email) {
        ResumeResponse r = new ResumeResponse();
        r.setResumeId(id);
        r.setUserEmail(email);
        r.setFileUrl("/uploads/resumes/test.pdf");
        r.setUploadedAt(LocalDateTime.now());
        return r;
    }

    // uploadResume tests

    @Test
    void uploadResume_success() {
        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFileUrl("/uploads/resumes/test.pdf");

        Resume saved = buildResume(1L, "seeker@test.com");
        ResumeResponse expected = buildResponse(1L, "seeker@test.com");

        when(resumeRepository.save(any(Resume.class))).thenReturn(saved);
        when(resumeMapper.toResponse(saved)).thenReturn(expected);

        ResumeResponse response = resumeService.uploadResume(request, "seeker@test.com", "JOB_SEEKER");

        assertNotNull(response);
        assertEquals(1L, response.getResumeId());
        assertEquals("seeker@test.com", response.getUserEmail());
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void uploadResume_notJobSeeker_throwsException() {
        ResumeUploadRequest request = new ResumeUploadRequest();

        UnauthorizedException ex = assertThrows(UnauthorizedException.class,
                () -> resumeService.uploadResume(request, "recruiter@test.com", "RECRUITER"));

        assertEquals("Only job seekers can upload resumes", ex.getMessage());
        verify(resumeRepository, never()).save(any(Resume.class));
    }

    // getResumeById tests

    @Test
    void getResumeById_ownResume_success() {
        Resume resume = buildResume(1L, "seeker@test.com");
        ResumeResponse expected = buildResponse(1L, "seeker@test.com");

        when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));
        when(resumeMapper.toResponse(resume)).thenReturn(expected);

        ResumeResponse response = resumeService.getResumeById(1L, "seeker@test.com", "JOB_SEEKER");

        assertNotNull(response);
        assertEquals(1L, response.getResumeId());
        verify(resumeRepository).findById(1L);
    }

    @Test
    void getResumeById_recruiterCanViewAny_success() {
        Resume resume = buildResume(1L, "seeker@test.com");
        ResumeResponse expected = buildResponse(1L, "seeker@test.com");

        when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));
        when(resumeMapper.toResponse(resume)).thenReturn(expected);

        ResumeResponse response = resumeService.getResumeById(1L, "recruiter@test.com", "RECRUITER");

        assertNotNull(response);
        verify(resumeRepository).findById(1L);
    }

    @Test
    void getResumeById_notFound_throwsException() {
        when(resumeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResumeNotFoundException.class,
                () -> resumeService.getResumeById(99L, "seeker@test.com", "JOB_SEEKER"));
    }

    @Test
    void getResumeById_otherUserResume_throwsException() {
        Resume resume = buildResume(1L, "other@test.com");
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));

        assertThrows(UnauthorizedException.class,
                () -> resumeService.getResumeById(1L, "seeker@test.com", "JOB_SEEKER"));
    }

    // getMyResumes tests

    @Test
    void getMyResumes_success() {
        Resume resume = buildResume(1L, "seeker@test.com");
        ResumeResponse expected = buildResponse(1L, "seeker@test.com");

        when(resumeRepository.findByUserEmail("seeker@test.com")).thenReturn(List.of(resume));
        when(resumeMapper.toResponse(resume)).thenReturn(expected);

        List<ResumeResponse> result = resumeService.getMyResumes("seeker@test.com");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(resumeRepository).findByUserEmail("seeker@test.com");
    }

    @Test
    void getMyResumes_empty_returnsEmptyList() {
        when(resumeRepository.findByUserEmail("seeker@test.com")).thenReturn(List.of());

        List<ResumeResponse> result = resumeService.getMyResumes("seeker@test.com");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // deleteResume tests

    @Test
    void deleteResume_success() {
        Resume resume = buildResume(1L, "seeker@test.com");
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));

        resumeService.deleteResume(1L, "seeker@test.com");

        verify(resumeRepository).delete(resume);
    }

    @Test
    void deleteResume_notFound_throwsException() {
        when(resumeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResumeNotFoundException.class,
                () -> resumeService.deleteResume(99L, "seeker@test.com"));

        verify(resumeRepository, never()).delete(any());
    }

    @Test
    void deleteResume_otherUserResume_throwsException() {
        Resume resume = buildResume(1L, "other@test.com");
        when(resumeRepository.findById(1L)).thenReturn(Optional.of(resume));

        assertThrows(UnauthorizedException.class,
                () -> resumeService.deleteResume(1L, "seeker@test.com"));

        verify(resumeRepository, never()).delete(any());
    }

    // uploadResumeFile tests

    @Test
    void uploadResumeFile_success(@TempDir Path tempDir) {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "pdf content".getBytes());

        Resume saved = buildResume(1L, "seeker@test.com");
        ResumeResponse expected = buildResponse(1L, "seeker@test.com");

        ReflectionTestUtils.setField(resumeService, "uploadDir", tempDir.toString());
        when(resumeRepository.save(any(Resume.class))).thenReturn(saved);
        when(resumeMapper.toResponse(saved)).thenReturn(expected);

        ResumeResponse response = resumeService.uploadResumeFile(file, "seeker@test.com", "JOB_SEEKER");

        assertNotNull(response);
        assertEquals(1L, response.getResumeId());
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void uploadResumeFile_docxExtension_success(@TempDir Path tempDir) {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "docx content".getBytes());

        Resume saved = buildResume(1L, "seeker@test.com");
        ResumeResponse expected = buildResponse(1L, "seeker@test.com");

        ReflectionTestUtils.setField(resumeService, "uploadDir", tempDir.toString());
        when(resumeRepository.save(any(Resume.class))).thenReturn(saved);
        when(resumeMapper.toResponse(saved)).thenReturn(expected);

        ResumeResponse response = resumeService.uploadResumeFile(file, "seeker@test.com", "JOB_SEEKER");

        assertNotNull(response);
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void uploadResumeFile_notJobSeeker_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "content".getBytes());

        assertThrows(UnauthorizedException.class,
                () -> resumeService.uploadResumeFile(file, "recruiter@test.com", "RECRUITER"));

        verify(resumeRepository, never()).save(any());
    }

    @Test
    void uploadResumeFile_emptyFile_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", new byte[0]);

        assertThrows(IllegalArgumentException.class,
                () -> resumeService.uploadResumeFile(file, "seeker@test.com", "JOB_SEEKER"));
    }

    @Test
    void uploadResumeFile_invalidExtension_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "content".getBytes());

        assertThrows(IllegalArgumentException.class,
                () -> resumeService.uploadResumeFile(file, "seeker@test.com", "JOB_SEEKER"));
    }

    @Test
    void uploadResumeFile_docExtension_success(@TempDir Path tempDir) {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.doc", "application/msword", "doc content".getBytes());

        Resume saved = buildResume(1L, "seeker@test.com");
        ResumeResponse expected = buildResponse(1L, "seeker@test.com");

        ReflectionTestUtils.setField(resumeService, "uploadDir", tempDir.toString());
        when(resumeRepository.save(any(Resume.class))).thenReturn(saved);
        when(resumeMapper.toResponse(saved)).thenReturn(expected);

        ResumeResponse response = resumeService.uploadResumeFile(file, "seeker@test.com", "JOB_SEEKER");

        assertNotNull(response);
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void uploadResumeFile_noExtension_throwsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "testfile", "application/octet-stream", "content".getBytes());

        assertThrows(IllegalArgumentException.class,
                () -> resumeService.uploadResumeFile(file, "seeker@test.com", "JOB_SEEKER"));
    }

    @Test
    void uploadResume_rabbitMqFailure_stillReturnsResponse() {
        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFileUrl("/uploads/resumes/test.pdf");

        Resume saved = buildResume(1L, "seeker@test.com");
        ResumeResponse expected = buildResponse(1L, "seeker@test.com");

        when(resumeRepository.save(any(Resume.class))).thenReturn(saved);
        when(resumeMapper.toResponse(saved)).thenReturn(expected);
        doThrow(new RuntimeException("RabbitMQ down"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

        ResumeResponse response = resumeService.uploadResume(request, "seeker@test.com", "JOB_SEEKER");

        assertNotNull(response);
        assertEquals(1L, response.getResumeId());
    }
}

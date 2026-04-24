package com.capg.resumeservice.controller;

import com.capg.resumeservice.dto.request.ResumeUploadRequest;
import com.capg.resumeservice.dto.response.ResumeResponse;
import com.capg.resumeservice.service.ResumeService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;

@WebMvcTest(ResumeController.class)
class ResumeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ResumeService resumeService;

    private ResumeResponse buildResponse() {
        ResumeResponse res = new ResumeResponse();
        res.setResumeId(1L);
        res.setUserEmail("seeker@test.com");
        res.setFileUrl("/uploads/test.pdf");
        res.setUploadedAt(LocalDateTime.now());
        return res;
    }

    @Test
    void uploadResume_returns200() throws Exception {
        ResumeUploadRequest request = new ResumeUploadRequest();
        request.setFileUrl("http://example.com/test.pdf");
        when(resumeService.uploadResume(any(), anyString(), anyString())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/resumes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-User-Email", "seeker@test.com")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk());
    }

    @Test
    void getResumeById_returns200() throws Exception {
        when(resumeService.getResumeById(anyLong(), anyString(), anyString())).thenReturn(buildResponse());

        mockMvc.perform(get("/api/resumes/1")
                .header("X-User-Email", "seeker@test.com")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk());
    }

    @Test
    void getMyResumes_returns200() throws Exception {
        when(resumeService.getMyResumes(anyString())).thenReturn(List.of(buildResponse()));

        mockMvc.perform(get("/api/resumes/me")
                .header("X-User-Email", "seeker@test.com"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteResume_returns204() throws Exception {
        doNothing().when(resumeService).deleteResume(anyLong(), anyString());

        mockMvc.perform(delete("/api/resumes/1")
                .header("X-User-Email", "seeker@test.com"))
                .andExpect(status().isNoContent());
    }

    @Test
    void uploadResumeFile_returns200() throws Exception {
        when(resumeService.uploadResumeFile(any(), anyString(), anyString())).thenReturn(buildResponse());

        mockMvc.perform(multipart("/api/resumes/upload")
                .file("file", "pdf content".getBytes())
                .header("X-User-Email", "seeker@test.com")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk());
    }
}

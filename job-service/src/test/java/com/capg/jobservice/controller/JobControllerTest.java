package com.capg.jobservice.controller;

import com.capg.jobservice.dto.request.JobRequest;
import com.capg.jobservice.dto.response.JobResponse;
import com.capg.jobservice.service.JobService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobController.class)
class JobControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private JobService jobService;

    private JobResponse buildResponse() {
        return new JobResponse(1L, "Java Developer", "TechCorp", "Bangalore",
                1000000.0, "Spring Boot", "OPEN", "recruiter@test.com", LocalDateTime.now());
    }

    @Test
    void createJob_returns200() throws Exception {
        JobRequest request = new JobRequest();
        request.setTitle("Java Developer");
        request.setCompany("TechCorp");
        request.setLocation("Bangalore");

        when(jobService.createJob(any(), anyString(), anyString())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-User-Email", "recruiter@test.com")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(1));
    }

    @Test
    void getJob_returns200() throws Exception {
        when(jobService.getJobById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/api/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Java Developer"));
    }

    @Test
    void getAllJobs_returns200() throws Exception {
        when(jobService.getAllJobs(0, 10))
                .thenReturn(new PageImpl<>(List.of(buildResponse()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/jobs"))
                .andExpect(status().isOk());
    }

    @Test
    void closeJob_returns200() throws Exception {
        JobResponse closed = new JobResponse(1L, "Java Developer", "TechCorp", "Bangalore",
                1000000.0, "Spring Boot", "CLOSED", "recruiter@test.com", LocalDateTime.now());
        when(jobService.closeJob(1L, "RECRUITER")).thenReturn(closed);

        mockMvc.perform(put("/api/jobs/1/close")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }
}

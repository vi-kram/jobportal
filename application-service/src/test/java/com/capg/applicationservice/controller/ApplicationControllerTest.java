package com.capg.applicationservice.controller;

import com.capg.applicationservice.dto.request.ApplicationRequest;
import com.capg.applicationservice.dto.response.ApplicationResponse;
import com.capg.applicationservice.entity.ApplicationStatus;
import com.capg.applicationservice.service.ApplicationService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApplicationController.class)
class ApplicationControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private ApplicationService service;

    private ApplicationResponse buildResponse() {
        return new ApplicationResponse(UUID.randomUUID(), 1L, "seeker@test.com",
                ApplicationStatus.APPLIED, LocalDateTime.now());
    }

    @Test
    void apply_returns200() throws Exception {
        ApplicationRequest request = new ApplicationRequest(1L);
        when(service.apply(any(), anyString(), anyString())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/applications")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-User-Email", "seeker@test.com")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk());
    }

    @Test
    void getMyApplications_returns200() throws Exception {
        when(service.getMyApplications(anyString(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(buildResponse()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/applications/me")
                .header("X-User-Email", "seeker@test.com"))
                .andExpect(status().isOk());
    }

    @Test
    void getApplicants_returns200() throws Exception {
        when(service.getApplicants(anyLong(), anyString(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(buildResponse()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/api/applications/job/1")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_returns200() throws Exception {
        ApplicationResponse updated = new ApplicationResponse(UUID.randomUUID(), 1L, "seeker@test.com",
                ApplicationStatus.SHORTLISTED, LocalDateTime.now());
        when(service.updateStatus(any(), anyString(), anyString())).thenReturn(updated);

        mockMvc.perform(put("/api/applications/" + UUID.randomUUID() + "/status")
                .param("status", "SHORTLISTED")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk());
    }
}

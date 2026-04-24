package com.capg.searchservice.controller;

import com.capg.searchservice.dto.JobSearchResponse;
import com.capg.searchservice.service.JobSearchService;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobSearchController.class)
class JobSearchControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private JobSearchService service;

    private JobSearchResponse buildResponse() {
        JobSearchResponse res = new JobSearchResponse();
        res.setJobId(1L);
        res.setTitle("Java Developer");
        res.setStatus("OPEN");
        return res;
    }

    @Test
    void getAllOpenJobs_returns200() throws Exception {
        when(service.getAllOpenJobs(0, 10))
                .thenReturn(new PageImpl<>(List.of(buildResponse()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/search/all/jobs"))
                .andExpect(status().isOk());
    }

    @Test
    void searchJobs_returns200() throws Exception {
        when(service.search(any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(new PageImpl<>(List.of(buildResponse()), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/search/jobs").param("keyword", "Java"))
                .andExpect(status().isOk());
    }

    @Test
    void getJobById_returns200() throws Exception {
        when(service.getJobById(1L)).thenReturn(buildResponse());

        mockMvc.perform(get("/search/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(1));
    }
}

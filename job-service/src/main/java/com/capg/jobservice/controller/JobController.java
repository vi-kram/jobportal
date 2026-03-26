package com.capg.jobservice.controller;

import com.capg.jobservice.dto.request.JobRequest;
import com.capg.jobservice.dto.response.JobResponse;
import com.capg.jobservice.entity.Job;
import com.capg.jobservice.service.JobService;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private static final Logger log = LoggerFactory.getLogger(JobController.class);

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping
    public ResponseEntity<JobResponse> createJob(
            @Valid @RequestBody JobRequest request,
            @RequestHeader("X-User-Email") String email) {
        log.info("POST /api/jobs recruiter={}", email);
        JobResponse response = jobService.createJob(request, email);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{jobId}")
    public ResponseEntity<JobResponse> getJob(@PathVariable Long jobId) {
        log.info("GET /api/jobs/{}", jobId);
        return ResponseEntity.ok(jobService.getJobById(jobId));
    }

    @GetMapping
    public ResponseEntity<Page<JobResponse>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("GET /api/jobs page={} size={}", page, size);
        return ResponseEntity.ok(jobService.getAllJobs(page, size));
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<Job> closeJob(@PathVariable Long id) {
        log.info("PUT /api/jobs/{}/close", id);
        Job job = jobService.closeJob(id);
        return ResponseEntity.ok(job);
    }
}
package com.capg.searchservice.controller;

import com.capg.searchservice.dto.JobSearchResponse;
import com.capg.searchservice.service.JobSearchService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
public class JobSearchController {

    private final JobSearchService service;

    public JobSearchController(JobSearchService service) {
        this.service = service;
    }

    @GetMapping("/all/jobs")
    public ResponseEntity<Page<JobSearchResponse>> getAllOpenJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.getAllOpenJobs(page, size));
    }

    @GetMapping("/jobs")
    public ResponseEntity<Page<JobSearchResponse>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) Double minSalary,
            @RequestParam(required = false) Double maxSalary,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.search(keyword, location, company, minSalary, maxSalary, page, size));
    }

    @GetMapping("/jobs/{jobId}")
    public ResponseEntity<JobSearchResponse> getJobById(@PathVariable Long jobId) {
        return ResponseEntity.ok(service.getJobById(jobId));
    }
}

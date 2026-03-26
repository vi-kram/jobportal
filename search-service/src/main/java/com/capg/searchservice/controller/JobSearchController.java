package com.capg.searchservice.controller;

import com.capg.searchservice.entity.Job;
import com.capg.searchservice.service.JobSearchService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
public class JobSearchController {

    private final JobSearchService service;

    public JobSearchController(JobSearchService service) {
        this.service = service;
    }

    @GetMapping("/jobs")
    public Page<Job> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.search(keyword, location, page, size);
    }

    @GetMapping("/jobs/skills")
    public Page<Job> searchBySkills(
            @RequestParam String skills,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return service.searchBySkills(skills, page, size);
    }
}
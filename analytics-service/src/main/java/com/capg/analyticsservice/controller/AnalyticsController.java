package com.capg.analyticsservice.controller;

import com.capg.analyticsservice.entity.JobApplicationMetrics;
import com.capg.analyticsservice.entity.UserApplicationMetrics;
import com.capg.analyticsservice.service.AnalyticsService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public Map<String, Long> getSummary() {
        return analyticsService.getSummary();
    }

    @GetMapping("/jobs")
    public Page<JobApplicationMetrics> getJobMetrics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return analyticsService.getJobMetrics(page, size);
    }

    @GetMapping("/users")
    public Page<UserApplicationMetrics> getUserMetrics(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return analyticsService.getUserMetrics(page, size);
    }
}

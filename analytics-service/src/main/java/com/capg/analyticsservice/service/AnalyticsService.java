package com.capg.analyticsservice.service;

import com.capg.analyticsservice.entity.JobApplicationMetrics;
import com.capg.analyticsservice.entity.MetricsSummary;
import com.capg.analyticsservice.entity.UserApplicationMetrics;
import com.capg.analyticsservice.repository.JobApplicationMetricsRepository;
import com.capg.analyticsservice.repository.MetricsSummaryRepository;
import com.capg.analyticsservice.repository.UserApplicationMetricsRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Service
public class AnalyticsService {

    private final MetricsSummaryRepository summaryRepo;
    private final JobApplicationMetricsRepository jobRepo;
    private final UserApplicationMetricsRepository userRepo;

    public AnalyticsService(MetricsSummaryRepository summaryRepo,
                            JobApplicationMetricsRepository jobRepo,
                            UserApplicationMetricsRepository userRepo) {
        this.summaryRepo = summaryRepo;
        this.jobRepo = jobRepo;
        this.userRepo = userRepo;
    }

    //  Increment Summary Metric
    @Transactional
    public void incrementMetric(String metricName) {

        MetricsSummary metric = summaryRepo
                .findByMetricName(metricName)
                .orElse(null);

        if (metric == null) {
            metric = new MetricsSummary();
            metric.setMetricName(metricName);
            metric.setCount(1L);
        } else {
            metric.setCount(metric.getCount() + 1);
        }

        summaryRepo.save(metric);
    }

    //  Increment Job Application Count
    @Transactional
    public void incrementJobApplication(Long jobId) {

        JobApplicationMetrics jobMetric = jobRepo
                .findById(jobId)
                .orElse(null);

        if (jobMetric == null) {
            jobMetric = new JobApplicationMetrics();
            jobMetric.setJobId(jobId);
            jobMetric.setApplicationCount(1L);
        } else {
            jobMetric.setApplicationCount(
                    jobMetric.getApplicationCount() + 1
            );
        }

        jobRepo.save(jobMetric);
    }

    //  Increment User Application Count (keyed by email)
    @Transactional
    public void incrementUserApplication(String userEmail) {

        UserApplicationMetrics userMetric = userRepo
                .findById(userEmail)
                .orElse(null);

        if (userMetric == null) {
            userMetric = new UserApplicationMetrics();
            userMetric.setUserEmail(userEmail);
            userMetric.setApplicationCount(1L);
        } else {
            userMetric.setApplicationCount(
                    userMetric.getApplicationCount() + 1
            );
        }

        userRepo.save(userMetric);
    }
    
    public Map<String, Long> getSummary() {
        List<MetricsSummary> list = summaryRepo.findAll();
        Map<String, Long> map = new HashMap<>();
        for (MetricsSummary m : list) {
            map.put(m.getMetricName(), m.getCount());
        }
        return map;
    }

    public Page<JobApplicationMetrics> getJobMetrics(int page, int size) {
        return jobRepo.findAll(PageRequest.of(page, size));
    }

    public Page<UserApplicationMetrics> getUserMetrics(int page, int size) {
        return userRepo.findAll(PageRequest.of(page, size));
    }
}
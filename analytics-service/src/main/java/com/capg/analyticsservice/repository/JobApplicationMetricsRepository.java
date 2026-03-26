package com.capg.analyticsservice.repository;

import com.capg.analyticsservice.entity.JobApplicationMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobApplicationMetricsRepository extends JpaRepository<JobApplicationMetrics, Long> {
}
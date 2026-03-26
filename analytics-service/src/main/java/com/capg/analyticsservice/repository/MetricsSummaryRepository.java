package com.capg.analyticsservice.repository;

import com.capg.analyticsservice.entity.MetricsSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MetricsSummaryRepository extends JpaRepository<MetricsSummary, Long> {

    Optional<MetricsSummary> findByMetricName(String metricName);
}
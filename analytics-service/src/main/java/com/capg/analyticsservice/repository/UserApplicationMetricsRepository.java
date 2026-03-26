package com.capg.analyticsservice.repository;

import com.capg.analyticsservice.entity.UserApplicationMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserApplicationMetricsRepository extends JpaRepository<UserApplicationMetrics, String> {
}
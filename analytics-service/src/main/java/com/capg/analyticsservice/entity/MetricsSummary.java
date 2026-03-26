package com.capg.analyticsservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "metrics_summary")
public class MetricsSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String metricName;

    private Long count;

    public MetricsSummary() {}

    public MetricsSummary(Long id, String metricName, Long count) {
        this.id = id;
        this.metricName = metricName;
        this.count = count;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public String getMetricName() {
        return metricName;
    }

    public Long getCount() {
        return count;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
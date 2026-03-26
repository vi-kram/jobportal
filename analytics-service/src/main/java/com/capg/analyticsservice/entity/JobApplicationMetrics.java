//Tracks applications per job

package com.capg.analyticsservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "job_application_metrics")
public class JobApplicationMetrics {

    @Id
    private Long jobId;

    private Long applicationCount;

    public JobApplicationMetrics() {}

    public JobApplicationMetrics(Long jobId, Long applicationCount) {
        this.jobId = jobId;
        this.applicationCount = applicationCount;
    }

    // Getters & Setters
    public Long getJobId() {
        return jobId;
    }

    public Long getApplicationCount() {
        return applicationCount;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public void setApplicationCount(Long applicationCount) {
        this.applicationCount = applicationCount;
    }
}
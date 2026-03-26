package com.capg.applicationservice.dto.response;

import com.capg.applicationservice.entity.ApplicationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class ApplicationResponse {

    private UUID applicationId;
    private Long jobId;
    private String userEmail;
    private ApplicationStatus status;
    private LocalDateTime appliedAt;

    public ApplicationResponse() {
    }

    public ApplicationResponse(UUID applicationId, Long jobId, String userEmail,
                               ApplicationStatus status, LocalDateTime appliedAt) {
        this.applicationId = applicationId;
        this.jobId = jobId;
        this.userEmail = userEmail;
        this.status = status;
        this.appliedAt = appliedAt;
    }

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public Long getJobId() {
        return jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public LocalDateTime getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(LocalDateTime appliedAt) {
        this.appliedAt = appliedAt;
    }
}
package com.capg.notificationservice.dto;

public class ApplicationEvent {

    private String applicationId;
    private Long jobId;
    private String userEmail;
    private String status;

    public ApplicationEvent() { /* default constructor for Jackson deserialization */ }

    public String getApplicationId() { return applicationId; }
    public void setApplicationId(String applicationId) { this.applicationId = applicationId; }

    public Long getJobId() { return jobId; }
    public void setJobId(Long jobId) { this.jobId = jobId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

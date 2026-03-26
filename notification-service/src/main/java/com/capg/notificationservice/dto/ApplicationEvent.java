package com.capg.notificationservice.dto;

public class ApplicationEvent {

    private String applicationId;
    private String userEmail;
    private String jobId;
    private String status;

    public ApplicationEvent() {
    }

    public ApplicationEvent(String applicationId, String userEmail, String jobId, String status) {
        this.applicationId = applicationId;
        this.userEmail = userEmail;
        this.jobId = jobId;
        this.status = status;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
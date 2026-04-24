package com.capg.notificationservice.dto;

public class ResumeEvent {

    private String resumeId;
    private String userEmail;
    private String fileUrl;

    public ResumeEvent() { /* default constructor for Jackson deserialization */ }

    public String getResumeId() { return resumeId; }
    public void setResumeId(String resumeId) { this.resumeId = resumeId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}

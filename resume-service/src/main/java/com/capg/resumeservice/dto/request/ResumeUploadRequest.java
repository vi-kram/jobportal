package com.capg.resumeservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ResumeUploadRequest {

    @NotNull(message = "UserId is required")
    private Long userId;

    @NotBlank(message = "File URL is required")
    private String fileUrl;

    @NotBlank(message = "User email is required")
    private String userEmail;

    public ResumeUploadRequest() {}

    public ResumeUploadRequest(Long userId, String fileUrl, String userEmail) {
        this.userId = userId;
        this.fileUrl = fileUrl;
        this.userEmail = userEmail;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
}
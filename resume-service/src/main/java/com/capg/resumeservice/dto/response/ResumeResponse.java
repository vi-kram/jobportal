package com.capg.resumeservice.dto.response;

import java.time.LocalDateTime;

public class ResumeResponse {

    private Long resumeId;
    private Long userId;
    private String fileUrl;
    private LocalDateTime uploadedAt;

    public ResumeResponse() {}

    public ResumeResponse(Long resumeId, Long userId, String fileUrl, LocalDateTime uploadedAt) {
        this.resumeId = resumeId;
        this.userId = userId;
        this.fileUrl = fileUrl;
        this.uploadedAt = uploadedAt;
    }

    public Long getResumeId() {
        return resumeId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
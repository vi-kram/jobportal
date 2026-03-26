package com.capg.resumeservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resumeId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String fileUrl;

    private LocalDateTime uploadedAt;

    // Constructors
    public Resume() {}

    public Resume(Long resumeId, Long userId, String fileUrl, LocalDateTime uploadedAt) {
        this.resumeId = resumeId;
        this.userId = userId;
        this.fileUrl = fileUrl;
        this.uploadedAt = uploadedAt;
    }

    // Getters and Setters
    public Long getResumeId() {
        return resumeId;
    }

    public void setResumeId(Long resumeId) {
        this.resumeId = resumeId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(LocalDateTime uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}
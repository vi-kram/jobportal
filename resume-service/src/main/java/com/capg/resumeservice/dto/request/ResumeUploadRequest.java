package com.capg.resumeservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;

public class ResumeUploadRequest {

    @NotBlank(message = "File URL is required")
    @URL(message = "File URL must be a valid URL")
    private String fileUrl;

    public ResumeUploadRequest() { /* default constructor for Jackson deserialization */ }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}

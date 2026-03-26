package com.capg.resumeservice.service;

import com.capg.resumeservice.dto.request.ResumeUploadRequest;
import com.capg.resumeservice.dto.response.ResumeResponse;

import java.util.List;

public interface ResumeService {

    ResumeResponse uploadResume(ResumeUploadRequest request);

    ResumeResponse getResumeById(Long resumeId);

    List<ResumeResponse> getResumesByUserId(Long userId);
}
package com.capg.resumeservice.service;

import com.capg.resumeservice.dto.request.ResumeUploadRequest;
import com.capg.resumeservice.dto.response.ResumeResponse;

import java.util.List;

public interface ResumeService {

    ResumeResponse uploadResume(ResumeUploadRequest request, String email, String role);

    ResumeResponse uploadResumeFile(org.springframework.web.multipart.MultipartFile file, String email, String role);

    ResumeResponse getResumeById(Long resumeId, String email, String role);

    List<ResumeResponse> getMyResumes(String email);

    List<ResumeResponse> getResumesByUserEmail(String userEmail, String requesterRole);

    void deleteResume(Long resumeId, String email);
}

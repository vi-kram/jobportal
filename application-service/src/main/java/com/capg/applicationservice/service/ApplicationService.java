package com.capg.applicationservice.service;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.capg.applicationservice.dto.request.ApplicationRequest;
import com.capg.applicationservice.dto.response.ApplicationResponse;

public interface ApplicationService {

	ApplicationResponse apply(ApplicationRequest request, String email, String role);

	Page<ApplicationResponse> getMyApplications(String email, int page, int size);

	Page<ApplicationResponse> getApplicants(Long jobId, String role, int page, int size);

	ApplicationResponse updateStatus(UUID applicationId, String status, String role);

	void withdrawApplication(UUID applicationId, String email);
}
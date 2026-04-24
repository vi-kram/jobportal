package com.capg.applicationservice.dto.request;

import jakarta.validation.constraints.NotNull;

public class ApplicationRequest {

	@NotNull(message = "Job ID is required")
	private Long jobId;

	public ApplicationRequest() {
	}

	public ApplicationRequest(Long jobId) {
		this.jobId = jobId;
	}

	public Long getJobId() {
		return jobId;
	}

	public void setJobId(Long jobId) {
		this.jobId = jobId;
	}
}
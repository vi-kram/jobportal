package com.capg.applicationservice.dto.request;

public class ApplicationRequest {

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
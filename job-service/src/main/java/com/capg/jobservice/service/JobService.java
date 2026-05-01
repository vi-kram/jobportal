package com.capg.jobservice.service;

import com.capg.jobservice.dto.request.JobRequest;
import com.capg.jobservice.dto.response.JobResponse;
import org.springframework.data.domain.Page;

public interface JobService {

	JobResponse createJob(JobRequest request, String recruiterEmail, String role);

	JobResponse updateJob(Long jobId, JobRequest request, String email);

	JobResponse getJobById(Long jobId);

    Page<JobResponse> getAllJobs(int page, int size);

    Page<JobResponse> getJobsByRecruiter(String email, int page, int size);

    JobResponse closeJob(Long jobId, String role);
}
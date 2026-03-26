package com.capg.jobservice.service;

import com.capg.jobservice.dto.request.JobRequest;
import com.capg.jobservice.dto.response.JobResponse;
import com.capg.jobservice.entity.Job;
import org.springframework.data.domain.Page;

public interface JobService {

	JobResponse createJob(JobRequest request, String recruiterEmail);

	JobResponse getJobById(Long jobId);

    Page<JobResponse> getAllJobs(int page, int size);

    Job closeJob(Long jobId);
}
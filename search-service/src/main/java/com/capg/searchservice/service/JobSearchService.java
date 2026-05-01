package com.capg.searchservice.service;

import com.capg.searchservice.dto.JobSearchResponse;
import com.capg.searchservice.entity.Job;
import com.capg.searchservice.exception.JobNotFoundException;
import com.capg.searchservice.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class JobSearchService {

    private static final String OPEN = "OPEN";

    private final JobRepository repository;

    public JobSearchService(JobRepository repository) {
        this.repository = repository;
    }

    public Page<JobSearchResponse> search(String keyword, String location, String company,
                                          Double minSalary, Double maxSalary, int page, int size) {
        boolean hasKeyword  = keyword != null && !keyword.isBlank();
        boolean hasLocation = location != null && !location.isBlank();
        boolean hasCompany  = company != null && !company.isBlank();
        boolean hasMin      = minSalary != null;
        boolean hasMax      = maxSalary != null;
        PageRequest pageable = PageRequest.of(page, size);

        if (hasMin || hasMax) {
            if (hasMin && hasMax)
                return repository.findByStatusAndSalaryBetween(OPEN, minSalary, maxSalary, pageable).map(this::toResponse);
            else if (hasMin)
                return repository.findByStatusAndSalaryGreaterThanEqual(OPEN, minSalary, pageable).map(this::toResponse);
            else
                return repository.findByStatusAndSalaryLessThanEqual(OPEN, maxSalary, pageable).map(this::toResponse);
        } else if (hasCompany) {
            return repository.findByStatusAndCompanyContainingIgnoreCase(OPEN, company, pageable).map(this::toResponse);
        } else if (hasKeyword && hasLocation) {
            return repository.findByStatusAndTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(
                    OPEN, keyword, location, pageable).map(this::toResponse);
        } else if (hasKeyword) {
            return repository.findByStatusAndTitleContainingIgnoreCase(OPEN, keyword, pageable).map(this::toResponse);
        } else if (hasLocation) {
            return repository.findByStatusAndLocationContainingIgnoreCase(OPEN, location, pageable).map(this::toResponse);
        } else {
            return repository.findByStatus(OPEN, pageable).map(this::toResponse);
        }
    }

    public JobSearchResponse getJobById(Long jobId) {
        return repository.findById(jobId)
                .map(this::toResponse)
                .orElseThrow(() -> new JobNotFoundException("Job not found in search index"));
    }

    public Page<JobSearchResponse> getAllOpenJobs(int page, int size) {
        return repository.findByStatus(OPEN, PageRequest.of(page, size)).map(this::toResponse);
    }

    private JobSearchResponse toResponse(Job job) {
        JobSearchResponse res = new JobSearchResponse();
        res.setJobId(job.getJobId());
        res.setTitle(job.getTitle());
        res.setDescription(job.getDescription());
        res.setCompany(job.getCompany());
        res.setLocation(job.getLocation());
        res.setSkills(job.getSkills());
        res.setSalary(job.getSalary());
        res.setStatus(job.getStatus());
        res.setJobType(job.getJobType());
        res.setExperienceLevel(job.getExperienceLevel());
        return res;
    }
}

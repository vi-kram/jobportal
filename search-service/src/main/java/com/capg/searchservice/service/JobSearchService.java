package com.capg.searchservice.service;

import com.capg.searchservice.entity.Job;
import com.capg.searchservice.repository.JobRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobSearchService {

    private final JobRepository repository;

    public JobSearchService(JobRepository repository) {
        this.repository = repository;
    }

    // Unified search with pagination
    public Page<Job> search(String keyword, String location, int page, int size) {

        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasLocation = location != null && !location.isBlank();
        PageRequest pageable = PageRequest.of(page, size);

        if (hasKeyword && hasLocation) {
            return repository.findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(keyword, location, pageable);
        } else if (hasKeyword) {
            return repository.findByTitleContainingIgnoreCase(keyword, pageable);
        } else if (hasLocation) {
            return repository.findByLocationContainingIgnoreCase(location, pageable);
        } else {
            return repository.findAll(pageable);
        }
    }

    public Page<Job> searchBySkills(String skills, int page, int size) {
        return repository.findBySkillsContainingIgnoreCase(skills, PageRequest.of(page, size));
    }
}
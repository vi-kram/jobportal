package com.capg.searchservice.repository;

import com.capg.searchservice.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Job> findByLocationContainingIgnoreCase(String location, Pageable pageable);

    Page<Job> findBySkillsContainingIgnoreCase(String skills, Pageable pageable);

    Page<Job> findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(
            String title, String location, Pageable pageable);
}
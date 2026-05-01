package com.capg.jobservice.repository;

import com.capg.jobservice.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    Page<Job> findByCreatedByOrderByCreatedAtDesc(String createdBy, Pageable pageable);
}
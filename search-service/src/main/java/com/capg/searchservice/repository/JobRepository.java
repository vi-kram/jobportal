package com.capg.searchservice.repository;

import com.capg.searchservice.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {

    Page<Job> findByStatusAndTitleContainingIgnoreCase(String status, String title, Pageable pageable);

    Page<Job> findByStatusAndLocationContainingIgnoreCase(String status, String location, Pageable pageable);

    Page<Job> findByStatusAndTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(
            String status, String title, String location, Pageable pageable);

    Page<Job> findByStatusAndSkillsContainingIgnoreCase(String status, String skills, Pageable pageable);

    Page<Job> findByStatusAndCompanyContainingIgnoreCase(String status, String company, Pageable pageable);

    Page<Job> findByStatus(String status, Pageable pageable);

    Page<Job> findByStatusAndSalaryBetween(String status, Double minSalary, Double maxSalary, Pageable pageable);

    Page<Job> findByStatusAndSalaryGreaterThanEqual(String status, Double minSalary, Pageable pageable);

    Page<Job> findByStatusAndSalaryLessThanEqual(String status, Double maxSalary, Pageable pageable);
}

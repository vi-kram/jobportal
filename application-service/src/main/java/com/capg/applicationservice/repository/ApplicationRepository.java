package com.capg.applicationservice.repository;

import com.capg.applicationservice.entity.Application;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {

    Page<Application> findByUserEmail(String userEmail, Pageable pageable);

    Page<Application> findByJobId(Long jobId, Pageable pageable);

    boolean existsByJobIdAndUserEmail(Long jobId, String userEmail);
}
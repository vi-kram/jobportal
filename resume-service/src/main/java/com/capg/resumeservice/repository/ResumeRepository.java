package com.capg.resumeservice.repository;

import com.capg.resumeservice.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByUserEmail(String userEmail);
}

package com.capg.resumeservice.service.impl;

import com.capg.resumeservice.config.RabbitMQConfig;
import com.capg.resumeservice.dto.ResumeEvent;
import com.capg.resumeservice.dto.request.ResumeUploadRequest;
import com.capg.resumeservice.dto.response.ResumeResponse;
import com.capg.resumeservice.entity.Resume;
import com.capg.resumeservice.exception.ResumeNotFoundException;
import com.capg.resumeservice.exception.UnauthorizedException;
import com.capg.resumeservice.repository.ResumeRepository;
import com.capg.resumeservice.service.ResumeService;
import com.capg.resumeservice.mapper.ResumeMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ResumeServiceImpl implements ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeServiceImpl.class);
    private static final String JOB_SEEKER = "JOB_SEEKER";

    private final ResumeRepository resumeRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ResumeMapper resumeMapper;

    @Value("${resume.upload.dir:uploads/resumes}")
    private String uploadDir;

    public ResumeServiceImpl(ResumeRepository resumeRepository, RabbitTemplate rabbitTemplate, ResumeMapper resumeMapper) {
        this.resumeRepository = resumeRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.resumeMapper = resumeMapper;
    }

    @Override
    @Transactional
    public ResumeResponse uploadResume(ResumeUploadRequest request, String email, String role) {
        if (!JOB_SEEKER.equals(role)) {
            log.warn("Unauthorized resume upload attempt");
            throw new UnauthorizedException("Only job seekers can upload resumes");
        }
        log.info("Uploading resume");

        Resume resume = new Resume();
        resume.setUserEmail(email);
        resume.setFileUrl(request.getFileUrl());
        resume.setUploadedAt(LocalDateTime.now());

        Resume saved = resumeRepository.save(resume);
        log.info("Resume saved resumeId={}", saved.getResumeId());

        publishResumeEvent(saved, email);

        return resumeMapper.toResponse(saved);
    }

    @Override
    public ResumeResponse getResumeById(Long resumeId, String email, String role) {
        log.debug("Fetching resume resumeId={}", resumeId);
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> {
                    log.warn("Resume not found resumeId={}", resumeId);
                    return new ResumeNotFoundException("Resume not found");
                });

        if (JOB_SEEKER.equals(role) && !resume.getUserEmail().equals(email)) {
            log.warn("Unauthorized resume access resumeId={}", resumeId);
            throw new UnauthorizedException("You can only view your own resumes");
        }

        return resumeMapper.toResponse(resume);
    }

    @Override
    public List<ResumeResponse> getMyResumes(String email) {
        log.debug("Fetching resumes");
        return resumeRepository.findByUserEmail(email)
                .stream()
                .map(resumeMapper::toResponse)
                .toList();
    }

    @Override
    public List<ResumeResponse> getResumesByUserEmail(String userEmail, String requesterRole) {
        log.debug("Fetching resumes for userEmail by requesterRole={}", requesterRole);
        if (!"RECRUITER".equals(requesterRole) && !"ADMIN".equals(requesterRole)) {
            throw new UnauthorizedException("Only recruiters and admins can view applicant resumes");
        }
        return resumeRepository.findByUserEmail(userEmail)
                .stream()
                .map(resumeMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ResumeResponse uploadResumeFile(MultipartFile file, String email, String role) {
        if (!JOB_SEEKER.equals(role)) {
            log.warn("Unauthorized resume file upload attempt");
            throw new UnauthorizedException("Only job seekers can upload resumes");
        }
        log.info("Uploading resume file");

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        String extension;
        String baseName;
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            baseName = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        } else {
            extension = "";
            baseName = originalFilename != null ? originalFilename : "resume";
        }

        if (!extension.equalsIgnoreCase(".pdf") && !extension.equalsIgnoreCase(".doc")
                && !extension.equalsIgnoreCase(".docx")) {
            throw new IllegalArgumentException("Only PDF, DOC, DOCX files are allowed");
        }

        Path uploadPath = Paths.get(uploadDir);
        try {
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Keep original filename, add timestamp only if file already exists
        String fileName = baseName + "_" + System.currentTimeMillis() + extension;
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        log.info("File saved as fileName={}", fileName);

        String fileUrl = "/uploads/resumes/" + fileName;

        Resume resume = new Resume();
        resume.setUserEmail(email);
        resume.setFileUrl(fileUrl);
        resume.setUploadedAt(LocalDateTime.now());

        Resume saved = resumeRepository.save(resume);
        log.info("Resume file saved resumeId={}", saved.getResumeId());

        publishResumeEvent(saved, email);

        return resumeMapper.toResponse(saved);
        } catch (IOException e) {
            log.error("File upload failed", e);
            throw new IllegalStateException("Failed to store file", e);
        }
    }

    private void publishResumeEvent(Resume saved, String email) {
        try {
            ResumeEvent event = new ResumeEvent(
                    saved.getResumeId().toString(),
                    email,
                    saved.getFileUrl()
            );
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.RESUME_KEY, event);
            log.info("RabbitMQ event published resumeId={}", saved.getResumeId());
        } catch (Exception e) {
            log.error("RabbitMQ publish failed resumeId={}", saved.getResumeId(), e);
        }
    }

    @Override
    @Transactional
    public void deleteResume(Long resumeId, String email) {
        log.info("Deleting resume resumeId={}", resumeId);
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found"));

        if (!resume.getUserEmail().equals(email)) {
            log.warn("Unauthorized delete attempt resumeId={}", resumeId);
            throw new UnauthorizedException("You can only delete your own resumes");
        }

        resumeRepository.delete(resume);
        log.info("Resume deleted resumeId={}", resumeId);
    }
}

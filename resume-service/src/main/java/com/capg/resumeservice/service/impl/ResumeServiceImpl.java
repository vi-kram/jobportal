package com.capg.resumeservice.service.impl;

import com.capg.resumeservice.config.RabbitMQConfig;
import com.capg.resumeservice.dto.ResumeEvent;
import com.capg.resumeservice.dto.request.ResumeUploadRequest;
import com.capg.resumeservice.dto.response.ResumeResponse;
import com.capg.resumeservice.entity.Resume;
import com.capg.resumeservice.repository.ResumeRepository;
import com.capg.resumeservice.service.ResumeService;

import com.capg.resumeservice.mapper.ResumeMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ResumeServiceImpl implements ResumeService {

    private static final Logger log = LoggerFactory.getLogger(ResumeServiceImpl.class);

    private final ResumeRepository resumeRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ResumeMapper resumeMapper;

    public ResumeServiceImpl(ResumeRepository resumeRepository, RabbitTemplate rabbitTemplate, ResumeMapper resumeMapper) {
        this.resumeRepository = resumeRepository;
        this.rabbitTemplate = rabbitTemplate;
        this.resumeMapper = resumeMapper;
    }

    @Override
    @Transactional
    public ResumeResponse uploadResume(ResumeUploadRequest request) {

        log.info("Uploading resume userId={} fileUrl={}", request.getUserId(), request.getFileUrl());

        Resume resume = new Resume();
        resume.setUserId(request.getUserId());
        resume.setFileUrl(request.getFileUrl());
        resume.setUploadedAt(LocalDateTime.now());

        Resume saved = resumeRepository.save(resume);
        log.info("Resume saved resumeId={} userId={}", saved.getResumeId(), saved.getUserId());

        ResumeEvent event = new ResumeEvent(
                saved.getResumeId().toString(),
                request.getUserEmail(),
                saved.getFileUrl()
        );

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.RESUME_KEY,
                event
        );
        log.info("RabbitMQ event published exchange={} routingKey={} resumeId={}", RabbitMQConfig.EXCHANGE, RabbitMQConfig.RESUME_KEY, saved.getResumeId());

        return mapToResponse(saved);
    }

    @Override
    public ResumeResponse getResumeById(Long resumeId) {

        log.debug("Fetching resume resumeId={}", resumeId);

        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> {
                    log.warn("Resume not found resumeId={}", resumeId);
                    return new RuntimeException("Resume not found");
                });

        return mapToResponse(resume);
    }

    @Override
    public List<ResumeResponse> getResumesByUserId(Long userId) {

        log.debug("Fetching resumes userId={}", userId);
        List<Resume> resumes = resumeRepository.findByUserId(userId);

        return resumes.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ResumeResponse mapToResponse(Resume resume) {
        return resumeMapper.toResponse(resume);
    }
}
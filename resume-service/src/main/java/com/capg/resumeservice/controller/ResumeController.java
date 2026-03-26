package com.capg.resumeservice.controller;

import com.capg.resumeservice.dto.request.ResumeUploadRequest;
import com.capg.resumeservice.dto.response.ResumeResponse;
import com.capg.resumeservice.service.ResumeService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    //  Upload Resume
    @PostMapping
    public ResponseEntity<ResumeResponse> uploadResume(
            @Valid @RequestBody ResumeUploadRequest request) {

        ResumeResponse response = resumeService.uploadResume(request);
        return ResponseEntity.ok(response);
    }

    //  Get Resume by ID
    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResumeById(@PathVariable Long id) {

        ResumeResponse response = resumeService.getResumeById(id);
        return ResponseEntity.ok(response);
    }

    //  Get Resumes by User
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ResumeResponse>> getResumesByUserId(
            @PathVariable Long userId) {

        List<ResumeResponse> responses = resumeService.getResumesByUserId(userId);
        return ResponseEntity.ok(responses);
    }
}
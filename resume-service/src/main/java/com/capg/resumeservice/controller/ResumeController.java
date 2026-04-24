package com.capg.resumeservice.controller;

import com.capg.resumeservice.dto.request.ResumeUploadRequest;
import com.capg.resumeservice.dto.response.ResumeResponse;
import com.capg.resumeservice.service.ResumeService;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping
    public ResponseEntity<ResumeResponse> uploadResume(
            @Valid @RequestBody ResumeUploadRequest request,
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email,
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(resumeService.uploadResume(request, email, role));
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<ResumeResponse> uploadResumeFile(
            @RequestParam("file") MultipartFile file,
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email,
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(resumeService.uploadResumeFile(file, email, role));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResumeResponse> getResumeById(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email,
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(resumeService.getResumeById(id, email, role));
    }

    @GetMapping("/me")
    public ResponseEntity<List<ResumeResponse>> getMyResumes(
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(resumeService.getMyResumes(email));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(
            @PathVariable Long id,
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email) {
        resumeService.deleteResume(id, email);
        return ResponseEntity.noContent().build();
    }
}

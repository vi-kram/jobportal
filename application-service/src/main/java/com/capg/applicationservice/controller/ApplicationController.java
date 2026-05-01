package com.capg.applicationservice.controller;

import com.capg.applicationservice.dto.request.ApplicationRequest;
import com.capg.applicationservice.dto.response.ApplicationResponse;
import com.capg.applicationservice.service.ApplicationService;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    private final ApplicationService service;

    public ApplicationController(ApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> apply(
            @Valid @RequestBody ApplicationRequest request,
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email,
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role
    ) {
        return ResponseEntity.ok(service.apply(request, email, role));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<ApplicationResponse>> getMyApplications(
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getMyApplications(email, page, size));
    }

    @GetMapping("/job/{jobId}")
    public ResponseEntity<Page<ApplicationResponse>> getApplicants(
            @PathVariable Long jobId,
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(service.getApplicants(jobId, role, page, size));
    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable UUID applicationId,
            @RequestParam String status,
            @Parameter(hidden = true) @RequestHeader("X-User-Role") String role
    ) {
        return ResponseEntity.ok(service.updateStatus(applicationId, status, role));
    }

    @DeleteMapping("/{applicationId}")
    public ResponseEntity<Void> withdraw(
            @PathVariable UUID applicationId,
            @Parameter(hidden = true) @RequestHeader("X-User-Email") String email
    ) {
        service.withdrawApplication(applicationId, email);
        return ResponseEntity.noContent().build();
    }
}

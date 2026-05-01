package com.capg.aiservice.controller;

import com.capg.aiservice.service.GeminiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final GeminiService geminiService;

    public AiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/analyze-resume")
    public ResponseEntity<Map<String, Object>> analyzeResume(
            @RequestBody Map<String, String> request,
            @RequestHeader("X-User-Role") String role) {
        if (!"JOB_SEEKER".equals(role)) {
            return ResponseEntity.status(403).build();
        }
        String fileUrl = request.get("fileUrl");
        if (fileUrl == null || fileUrl.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(geminiService.analyzeResume(fileUrl));
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, Object>> chat(
            @RequestBody Map<String, Object> request,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String email) {
        String message = (String) request.get("message");
        String context = (String) request.get("context");
        if (message == null || message.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(geminiService.chat(message, context, role, email));
    }
}

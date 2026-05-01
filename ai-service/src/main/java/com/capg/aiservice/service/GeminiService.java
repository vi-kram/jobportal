package com.capg.aiservice.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.net.URL;
import java.util.*;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    @Value("${resume.service.url}")
    private String resumeServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> analyzeResume(String fileUrl) {
        try {
            String resumeText = extractTextFromResume(fileUrl);
            if (resumeText == null || resumeText.isBlank()) {
                return errorResponse("Could not extract text from resume. Make sure it is a text-based PDF.");
            }
            return callGemini(resumeText);
        } catch (Exception e) {
            log.error("Resume analysis failed", e);
            return errorResponse("Analysis failed: " + e.getMessage());
        }
    }

    private String extractTextFromResume(String fileUrl) throws Exception {
        String downloadUrl = resumeServiceUrl + "/api/resumes/download/" + fileUrl.substring(fileUrl.lastIndexOf('/') + 1);
        log.info("Downloading resume from {}", downloadUrl);
        byte[] bytes = restTemplate.getForObject(downloadUrl, byte[].class);
        if (bytes == null) throw new RuntimeException("Empty file");
        try (PDDocument doc = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(doc);
        }
    }

    private Map<String, Object> callGemini(String resumeText) {
        String prompt = """
                Analyze the following resume and respond ONLY with a valid JSON object in this exact format:
                {
                  "score": <number 0-100>,
                  "summary": "<one sentence overall assessment>",
                  "strengths": ["<strength1>", "<strength2>", "<strength3>"],
                  "improvements": ["<improvement1>", "<improvement2>", "<improvement3>"],
                  "missingKeywords": ["<keyword1>", "<keyword2>", "<keyword3>"],
                  "recommendation": "<one actionable recommendation>"
                }
                
                Resume:
                """ + resumeText.substring(0, Math.min(resumeText.length(), 3000));

        Map<String, Object> requestBody = Map.of(
            "contents", List.of(Map.of(
                "parts", List.of(Map.of("text", prompt))
            ))
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.exchange(
            apiUrl + "?key=" + apiKey,
            HttpMethod.POST,
            new HttpEntity<>(requestBody, headers),
            Map.class
        );

        String rawText = extractTextFromGeminiResponse(response.getBody());
        log.info("Gemini raw response: {}", rawText);
        return parseJsonResponse(rawText);
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromGeminiResponse(Map body) {
        try {
            List candidates = (List) body.get("candidates");
            Map candidate = (Map) candidates.get(0);
            Map content = (Map) candidate.get("content");
            List parts = (List) content.get("parts");
            Map part = (Map) parts.get(0);
            return (String) part.get("text");
        } catch (Exception e) {
            log.error("Failed to extract text from Gemini response", e);
            return "{}";
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJsonResponse(String text) {
        try {
            String json = text.replaceAll("```json", "").replaceAll("```", "").trim();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            return mapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("Failed to parse Gemini JSON response: {}", e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("score", 50);
            fallback.put("summary", text);
            fallback.put("strengths", java.util.List.of());
            fallback.put("improvements", java.util.List.of());
            fallback.put("missingKeywords", java.util.List.of());
            fallback.put("recommendation", "");
            return fallback;
        }
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("score", 0);
        error.put("summary", message);
        error.put("strengths", List.of());
        error.put("improvements", List.of());
        error.put("missingKeywords", List.of());
        error.put("recommendation", "");
        return error;
    }

    public Map<String, Object> chat(String message, String context, String role, String email) {
        try {
            String prompt = "You are a helpful assistant for a job portal called JobPortal. " +
                "You have access to the following real-time data about the user (" + email + ") with role " + role + ":\n\n" +
                context + "\n\n" +
                "Answer the following question concisely and helpfully based on the data above. " +
                "If the data doesn't contain the answer, say so politely. Do not make up data.\n\n" +
                "User question: " + message;

            Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                    "parts", List.of(Map.of("text", prompt))
                ))
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<Map> response = restTemplate.exchange(
                apiUrl + "?key=" + apiKey,
                HttpMethod.POST,
                new HttpEntity<>(requestBody, headers),
                Map.class
            );

            String reply = extractTextFromGeminiResponse(response.getBody());
            Map<String, Object> result = new HashMap<>();
            result.put("reply", reply);
            return result;
        } catch (Exception e) {
            log.error("Chat failed", e);
            Map<String, Object> error = new HashMap<>();
            error.put("reply", "Sorry, I couldn't process your request. Please try again.");
            return error;
        }
    }
}

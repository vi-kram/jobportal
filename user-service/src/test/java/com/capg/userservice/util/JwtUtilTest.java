package com.capg.userservice.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "mySecretKeyForTestingPurposesOnly1234567890");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 3600000L);
    }

    @Test
    void generateToken_returnsNonNullToken() {
        String token = jwtUtil.generateToken("test@example.com", "JOB_SEEKER");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = jwtUtil.generateToken("test@example.com", "JOB_SEEKER");
        assertEquals("test@example.com", jwtUtil.extractEmail(token));
    }

    @Test
    void extractRole_returnsCorrectRole() {
        String token = jwtUtil.generateToken("test@example.com", "RECRUITER");
        assertEquals("RECRUITER", jwtUtil.extractRole(token));
    }

    @Test
    void validateToken_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("test@example.com", "JOB_SEEKER");
        assertTrue(jwtUtil.validateToken(token, "test@example.com"));
    }

    @Test
    void validateToken_wrongEmail_returnsFalse() {
        String token = jwtUtil.generateToken("test@example.com", "JOB_SEEKER");
        assertFalse(jwtUtil.validateToken(token, "other@example.com"));
    }

    @Test
    void validateToken_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String token = jwtUtil.generateToken("test@example.com", "JOB_SEEKER");
        assertFalse(jwtUtil.validateToken(token, "test@example.com"));
    }

    @Test
    void extractRole_adminRole_returnsCorrectRole() {
        String token = jwtUtil.generateToken("admin@example.com", "ADMIN");
        assertEquals("ADMIN", jwtUtil.extractRole(token));
    }

    @Test
    void validateToken_emailMismatch_returnsFalseWithoutCheckingExpiry() {
        String token = jwtUtil.generateToken("test@example.com", "JOB_SEEKER");
        // email doesn't match — short-circuit, isTokenExpired never evaluated
        assertFalse(jwtUtil.validateToken(token, "different@example.com"));
    }

    @Test
    void validateToken_recruiterRole_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("recruiter@example.com", "RECRUITER");
        assertTrue(jwtUtil.validateToken(token, "recruiter@example.com"));
        assertEquals("RECRUITER", jwtUtil.extractRole(token));
    }

    @Test
    void validateToken_adminRole_validToken_returnsTrue() {
        String token = jwtUtil.generateToken("admin@example.com", "ADMIN");
        assertTrue(jwtUtil.validateToken(token, "admin@example.com"));
    }
}

package com.capg.userservice.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleEmailExists_returns400() {
        ResponseEntity<ErrorResponse> response = handler.handleEmailExists(new EmailAlreadyExistsException("Email exists"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Email exists", response.getBody().getMessage());
    }

    @Test
    void handleUserNotFound_returns404() {
        ResponseEntity<ErrorResponse> response = handler.handleUserNotFound(new UserNotFoundException("Not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().getMessage());
    }

    @Test
    void handleInvalidCredentials_returns401() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentials(new InvalidCredentialsException("Invalid"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid", response.getBody().getMessage());
    }

    @Test
    void handleUnauthorized_returns403() {
        ResponseEntity<ErrorResponse> response = handler.handleUnauthorized(new UnauthorizedException("Forbidden"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Forbidden", response.getBody().getMessage());
    }

    @Test
    void handleGeneric_returns500() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(new Exception("Error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Error", response.getBody().getMessage());
    }

    @Test
    void handleInvalidEnum_returns400() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidEnum(
                new org.springframework.http.converter.HttpMessageNotReadableException("Invalid"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid value provided for role", response.getBody().getMessage());
    }
}

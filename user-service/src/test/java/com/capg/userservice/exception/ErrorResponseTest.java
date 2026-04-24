package com.capg.userservice.exception;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void errorResponse_gettersSettersWork() {
        LocalDateTime now = LocalDateTime.now();
        ErrorResponse response = new ErrorResponse(now, 400, "Bad Request", "Invalid input");

        assertEquals(now, response.getTimestamp());
        assertEquals(400, response.getStatus());
        assertEquals("Bad Request", response.getError());
        assertEquals("Invalid input", response.getMessage());

        response.setTimestamp(now);
        response.setStatus(500);
        response.setError("Server Error");
        response.setMessage("Something went wrong");

        assertEquals(500, response.getStatus());
        assertEquals("Server Error", response.getError());
        assertEquals("Something went wrong", response.getMessage());
    }

    @Test
    void errorResponse_noArgConstructor_works() {
        ErrorResponse response = new ErrorResponse();
        assertNotNull(response);
        LocalDateTime now = LocalDateTime.now();
        response.setTimestamp(now);
        response.setStatus(404);
        response.setError("Not Found");
        response.setMessage("Resource not found");
        assertEquals(404, response.getStatus());
        assertEquals("Not Found", response.getError());
    }
}

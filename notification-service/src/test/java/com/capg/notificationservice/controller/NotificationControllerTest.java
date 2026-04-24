package com.capg.notificationservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.*;

class NotificationControllerTest {

    private final NotificationController controller = new NotificationController();

    @Test
    void health_returnsOk() {
        ResponseEntity<String> response = controller.health();
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Notification service is running", response.getBody());
    }
}

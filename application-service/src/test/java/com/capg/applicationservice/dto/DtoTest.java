package com.capg.applicationservice.dto;

import com.capg.applicationservice.dto.ApplicationEvent;
import com.capg.applicationservice.dto.request.ApplicationRequest;
import com.capg.applicationservice.dto.response.ApplicationResponse;
import com.capg.applicationservice.entity.ApplicationStatus;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void applicationEvent_gettersSettersWork() {
        ApplicationEvent event = new ApplicationEvent("app-123", 1L, "seeker@test.com", "APPLIED");
        assertEquals("app-123", event.getApplicationId());
        assertEquals(1L, event.getJobId());
        assertEquals("seeker@test.com", event.getUserEmail());
        assertEquals("APPLIED", event.getStatus());

        event.setApplicationId("app-456");
        event.setJobId(2L);
        event.setUserEmail("other@test.com");
        event.setStatus("SHORTLISTED");
        assertEquals("app-456", event.getApplicationId());
        assertEquals(2L, event.getJobId());
    }

    @Test
    void applicationRequest_gettersSettersWork() {
        ApplicationRequest req = new ApplicationRequest(1L);
        assertEquals(1L, req.getJobId());

        req.setJobId(2L);
        assertEquals(2L, req.getJobId());
    }

    @Test
    void applicationResponse_gettersSettersWork() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        ApplicationResponse res = new ApplicationResponse(id, 1L, "seeker@test.com", ApplicationStatus.APPLIED, now);

        assertEquals(id, res.getApplicationId());
        assertEquals(1L, res.getJobId());
        assertEquals("seeker@test.com", res.getUserEmail());
        assertEquals(ApplicationStatus.APPLIED, res.getStatus());
        assertEquals(now, res.getAppliedAt());

        UUID newId = UUID.randomUUID();
        res.setApplicationId(newId);
        res.setJobId(2L);
        res.setUserEmail("other@test.com");
        res.setStatus(ApplicationStatus.SHORTLISTED);
        res.setAppliedAt(now);

        assertEquals(newId, res.getApplicationId());
        assertEquals(2L, res.getJobId());
        assertEquals(ApplicationStatus.SHORTLISTED, res.getStatus());
    }
}

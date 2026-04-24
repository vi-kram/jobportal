package com.capg.jobservice.dto;

import com.capg.jobservice.dto.response.JobResponse;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void jobEvent_gettersSettersWork() {
        JobEvent event = new JobEvent();
        event.setJobId(1L);
        event.setTitle("Java Developer");
        event.setDescription("Spring Boot");
        event.setCompany("TechCorp");
        event.setLocation("Bangalore");
        event.setSalary(1000000.0);
        event.setCreatedBy("recruiter@test.com");

        assertEquals(1L, event.getJobId());
        assertEquals("Java Developer", event.getTitle());
        assertEquals("Spring Boot", event.getDescription());
        assertEquals("TechCorp", event.getCompany());
        assertEquals("Bangalore", event.getLocation());
        assertEquals(1000000.0, event.getSalary());
        assertEquals("recruiter@test.com", event.getCreatedBy());
    }

    @Test
    void jobClosedEvent_gettersSettersWork() {
        JobClosedEvent event = new JobClosedEvent();
        event.setJobId(1L);
        event.setTitle("Java Developer");
        event.setStatus("CLOSED");
        event.setCreatedBy("recruiter@test.com");

        assertEquals(1L, event.getJobId());
        assertEquals("Java Developer", event.getTitle());
        assertEquals("CLOSED", event.getStatus());
        assertEquals("recruiter@test.com", event.getCreatedBy());
    }

    @Test
    void jobResponse_gettersSettersWork() {
        LocalDateTime now = LocalDateTime.now();
        JobResponse res = new JobResponse(1L, "Java Developer", "TechCorp", "Bangalore",
                1000000.0, "Spring Boot", "OPEN", "recruiter@test.com", now);

        assertEquals(1L, res.getJobId());
        assertEquals("Java Developer", res.getTitle());
        assertEquals("TechCorp", res.getCompany());
        assertEquals("Bangalore", res.getLocation());
        assertEquals(1000000.0, res.getSalary());
        assertEquals("Spring Boot", res.getDescription());
        assertEquals("OPEN", res.getStatus());
        assertEquals("recruiter@test.com", res.getCreatedBy());
        assertEquals(now, res.getCreatedAt());

        res.setJobId(2L);
        res.setTitle("Python Developer");
        assertEquals(2L, res.getJobId());
        assertEquals("Python Developer", res.getTitle());
    }

    @Test
    void jobResponse_noArgConstructor_works() {
        JobResponse res = new JobResponse();
        assertNotNull(res);
        res.setJobId(1L);
        res.setTitle("Java Developer");
        assertEquals(1L, res.getJobId());
        assertEquals("Java Developer", res.getTitle());
    }
}

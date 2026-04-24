package com.capg.notificationservice.dto;

import org.junit.jupiter.api.Test;
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
    void applicationEvent_gettersSettersWork() {
        ApplicationEvent event = new ApplicationEvent();
        event.setApplicationId("app-123");
        event.setJobId(1L);
        event.setUserEmail("seeker@test.com");
        event.setStatus("APPLIED");

        assertEquals("app-123", event.getApplicationId());
        assertEquals(1L, event.getJobId());
        assertEquals("seeker@test.com", event.getUserEmail());
        assertEquals("APPLIED", event.getStatus());
    }

    @Test
    void resumeEvent_gettersSettersWork() {
        ResumeEvent event = new ResumeEvent();
        event.setResumeId("resume-123");
        event.setUserEmail("seeker@test.com");
        event.setFileUrl("/uploads/test.pdf");

        assertEquals("resume-123", event.getResumeId());
        assertEquals("seeker@test.com", event.getUserEmail());
        assertEquals("/uploads/test.pdf", event.getFileUrl());
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
}

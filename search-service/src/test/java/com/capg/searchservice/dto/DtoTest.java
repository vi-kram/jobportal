package com.capg.searchservice.dto;

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
        event.setSkills("Java, Spring");
        event.setSalary(1000000.0);

        assertEquals(1L, event.getJobId());
        assertEquals("Java Developer", event.getTitle());
        assertEquals("Spring Boot", event.getDescription());
        assertEquals("TechCorp", event.getCompany());
        assertEquals("Bangalore", event.getLocation());
        assertEquals("Java, Spring", event.getSkills());
        assertEquals(1000000.0, event.getSalary());
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
    void jobSearchResponse_gettersSettersWork() {
        JobSearchResponse res = new JobSearchResponse();
        res.setJobId(1L);
        res.setTitle("Java Developer");
        res.setDescription("Spring Boot");
        res.setCompany("TechCorp");
        res.setLocation("Bangalore");
        res.setSkills("Java, Spring");
        res.setSalary(1000000.0);
        res.setStatus("OPEN");

        assertEquals(1L, res.getJobId());
        assertEquals("Java Developer", res.getTitle());
        assertEquals("Spring Boot", res.getDescription());
        assertEquals("TechCorp", res.getCompany());
        assertEquals("Bangalore", res.getLocation());
        assertEquals("Java, Spring", res.getSkills());
        assertEquals(1000000.0, res.getSalary());
        assertEquals("OPEN", res.getStatus());
    }
}

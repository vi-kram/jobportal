package com.capg.searchservice.service;

import com.capg.searchservice.dto.JobSearchResponse;
import com.capg.searchservice.entity.Job;
import com.capg.searchservice.exception.JobNotFoundException;
import com.capg.searchservice.repository.JobRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobSearchServiceTest {

    @Mock private JobRepository repository;

    @InjectMocks
    private JobSearchService jobSearchService;

    private Job buildJob(Long id, String title, String location, String company, Double salary) {
        Job job = new Job();
        job.setJobId(id);
        job.setTitle(title);
        job.setDescription("Test description");
        job.setCompany(company);
        job.setLocation(location);
        job.setSalary(salary);
        job.setStatus("OPEN");
        return job;
    }

    @Test
    void search_noFilters_returnsAllOpenJobs() {
        Job job = buildJob(1L, "Java Developer", "Bangalore", "TechCorp", 1000000.0);
        Page<Job> page = new PageImpl<>(List.of(job), PageRequest.of(0, 10), 1);
        when(repository.findByStatus("OPEN", PageRequest.of(0, 10))).thenReturn(page);

        Page<JobSearchResponse> result = jobSearchService.search(null, null, null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Java Developer", result.getContent().get(0).getTitle());
    }

    @Test
    void search_byKeyword_returnsMatchingJobs() {
        Job job = buildJob(1L, "Java Developer", "Bangalore", "TechCorp", 1000000.0);
        Page<Job> page = new PageImpl<>(List.of(job), PageRequest.of(0, 10), 1);
        when(repository.findByStatusAndTitleContainingIgnoreCase(eq("OPEN"), eq("Java"), any())).thenReturn(page);

        Page<JobSearchResponse> result = jobSearchService.search("Java", null, null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void search_byLocation_returnsMatchingJobs() {
        Job job = buildJob(1L, "Java Developer", "Bangalore", "TechCorp", 1000000.0);
        Page<Job> page = new PageImpl<>(List.of(job), PageRequest.of(0, 10), 1);
        when(repository.findByStatusAndLocationContainingIgnoreCase(eq("OPEN"), eq("Bangalore"), any())).thenReturn(page);

        Page<JobSearchResponse> result = jobSearchService.search(null, "Bangalore", null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void search_byKeywordAndLocation_returnsMatchingJobs() {
        Job job = buildJob(1L, "Java Developer", "Bangalore", "TechCorp", 1000000.0);
        Page<Job> page = new PageImpl<>(List.of(job), PageRequest.of(0, 10), 1);
        when(repository.findByStatusAndTitleContainingIgnoreCaseAndLocationContainingIgnoreCase(
                eq("OPEN"), eq("Java"), eq("Bangalore"), any())).thenReturn(page);

        Page<JobSearchResponse> result = jobSearchService.search("Java", "Bangalore", null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void search_byCompany_returnsMatchingJobs() {
        Job job = buildJob(1L, "Java Developer", "Bangalore", "TechCorp", 1000000.0);
        Page<Job> page = new PageImpl<>(List.of(job), PageRequest.of(0, 10), 1);
        when(repository.findByStatusAndCompanyContainingIgnoreCase(eq("OPEN"), eq("TechCorp"), any())).thenReturn(page);

        Page<JobSearchResponse> result = jobSearchService.search(null, null, "TechCorp", null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void search_byMinAndMaxSalary_returnsMatchingJobs() {
        Job job = buildJob(1L, "Java Developer", "Bangalore", "TechCorp", 1000000.0);
        Page<Job> page = new PageImpl<>(List.of(job), PageRequest.of(0, 10), 1);
        when(repository.findByStatusAndSalaryBetween(eq("OPEN"), eq(500000.0), eq(1500000.0), any())).thenReturn(page);

        Page<JobSearchResponse> result = jobSearchService.search(null, null, null, 500000.0, 1500000.0, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void search_byMinSalaryOnly_returnsMatchingJobs() {
        Job job = buildJob(1L, "Java Developer", "Bangalore", "TechCorp", 1000000.0);
        Page<Job> page = new PageImpl<>(List.of(job), PageRequest.of(0, 10), 1);
        when(repository.findByStatusAndSalaryGreaterThanEqual(eq("OPEN"), eq(500000.0), any())).thenReturn(page);

        Page<JobSearchResponse> result = jobSearchService.search(null, null, null, 500000.0, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void search_byMaxSalaryOnly_returnsMatchingJobs() {
        Job job = buildJob(1L, "Java Developer", "Bangalore", "TechCorp", 1000000.0);
        Page<Job> page = new PageImpl<>(List.of(job), PageRequest.of(0, 10), 1);
        when(repository.findByStatusAndSalaryLessThanEqual(eq("OPEN"), eq(1500000.0), any())).thenReturn(page);

        Page<JobSearchResponse> result = jobSearchService.search(null, null, null, null, 1500000.0, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getJobById_success() {
        Job job = buildJob(1L, "Java Developer", "Bangalore", "TechCorp", 1000000.0);
        when(repository.findById(1L)).thenReturn(Optional.of(job));

        JobSearchResponse result = jobSearchService.getJobById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getJobId());
        assertEquals("Java Developer", result.getTitle());
    }

    @Test
    void getJobById_notFound_throwsException() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(JobNotFoundException.class, () -> jobSearchService.getJobById(99L));
    }

    @Test
    void getAllOpenJobs_returnsPage() {
        Job job = buildJob(1L, "Java Developer", "Bangalore", "TechCorp", 1000000.0);
        Page<Job> page = new PageImpl<>(List.of(job), PageRequest.of(0, 10), 1);
        when(repository.findByStatus("OPEN", PageRequest.of(0, 10))).thenReturn(page);

        Page<JobSearchResponse> result = jobSearchService.getAllOpenJobs(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void search_blankKeyword_treatedAsNoKeyword() {
        Job job = buildJob(1L, "Java Developer", "Bangalore", "TechCorp", 1000000.0);
        Page<Job> page = new PageImpl<>(List.of(job), PageRequest.of(0, 10), 1);
        when(repository.findByStatusAndLocationContainingIgnoreCase(eq("OPEN"), eq("Bangalore"), any())).thenReturn(page);

        Page<JobSearchResponse> result = jobSearchService.search("  ", "Bangalore", null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void search_blankLocation_treatedAsNoLocation() {
        Job job = buildJob(1L, "Java Developer", "Bangalore", "TechCorp", 1000000.0);
        Page<Job> page = new PageImpl<>(List.of(job), PageRequest.of(0, 10), 1);
        when(repository.findByStatusAndTitleContainingIgnoreCase(eq("OPEN"), eq("Java"), any())).thenReturn(page);

        Page<JobSearchResponse> result = jobSearchService.search("Java", "  ", null, null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void search_blankCompany_treatedAsNoCompany() {
        Job job = buildJob(1L, "Java Developer", "Bangalore", "TechCorp", 1000000.0);
        Page<Job> page = new PageImpl<>(List.of(job), PageRequest.of(0, 10), 1);
        when(repository.findByStatus("OPEN", PageRequest.of(0, 10))).thenReturn(page);

        Page<JobSearchResponse> result = jobSearchService.search(null, null, "  ", null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}

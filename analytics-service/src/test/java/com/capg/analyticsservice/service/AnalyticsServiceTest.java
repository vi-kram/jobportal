package com.capg.analyticsservice.service;

import com.capg.analyticsservice.entity.JobApplicationMetrics;
import com.capg.analyticsservice.entity.MetricsSummary;
import com.capg.analyticsservice.entity.UserApplicationMetrics;
import com.capg.analyticsservice.repository.JobApplicationMetricsRepository;
import com.capg.analyticsservice.repository.MetricsSummaryRepository;
import com.capg.analyticsservice.repository.UserApplicationMetricsRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private MetricsSummaryRepository summaryRepo;

    @Mock
    private JobApplicationMetricsRepository jobRepo;

    @Mock
    private UserApplicationMetricsRepository userRepo;

    @InjectMocks
    private AnalyticsService analyticsService;

    // =============================================
    // incrementMetric tests
    // =============================================

    @Test
    void incrementMetric_newMetric_createsWithCountOne() {
        // Arrange
        when(summaryRepo.findByMetricName("JOB_CREATED")).thenReturn(Optional.empty());
        when(summaryRepo.save(any(MetricsSummary.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        analyticsService.incrementMetric("JOB_CREATED");

        // Assert
        ArgumentCaptor<MetricsSummary> captor = ArgumentCaptor.forClass(MetricsSummary.class);
        verify(summaryRepo).save(captor.capture());
        assertEquals("JOB_CREATED", captor.getValue().getMetricName());
        assertEquals(1L, captor.getValue().getCount());
    }

    @Test
    void incrementMetric_existingMetric_incrementsCount() {
        // Arrange
        MetricsSummary existing = new MetricsSummary(1L, "JOB_CREATED", 5L);
        when(summaryRepo.findByMetricName("JOB_CREATED")).thenReturn(Optional.of(existing));
        when(summaryRepo.save(any(MetricsSummary.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        analyticsService.incrementMetric("JOB_CREATED");

        // Assert
        ArgumentCaptor<MetricsSummary> captor = ArgumentCaptor.forClass(MetricsSummary.class);
        verify(summaryRepo).save(captor.capture());
        assertEquals(6L, captor.getValue().getCount());
    }

    // =============================================
    // incrementJobApplication tests
    // =============================================

    @Test
    void incrementJobApplication_newJob_createsWithCountOne() {
        // Arrange
        when(jobRepo.findById(1L)).thenReturn(Optional.empty());
        when(jobRepo.save(any(JobApplicationMetrics.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        analyticsService.incrementJobApplication(1L);

        // Assert
        ArgumentCaptor<JobApplicationMetrics> captor = ArgumentCaptor.forClass(JobApplicationMetrics.class);
        verify(jobRepo).save(captor.capture());
        assertEquals(1L, captor.getValue().getJobId());
        assertEquals(1L, captor.getValue().getApplicationCount());
    }

    @Test
    void incrementJobApplication_existingJob_incrementsCount() {
        // Arrange
        JobApplicationMetrics existing = new JobApplicationMetrics(1L, 3L);
        when(jobRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(jobRepo.save(any(JobApplicationMetrics.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        analyticsService.incrementJobApplication(1L);

        // Assert
        ArgumentCaptor<JobApplicationMetrics> captor = ArgumentCaptor.forClass(JobApplicationMetrics.class);
        verify(jobRepo).save(captor.capture());
        assertEquals(4L, captor.getValue().getApplicationCount());
    }

    // =============================================
    // incrementUserApplication tests
    // =============================================

    @Test
    void incrementUserApplication_newUser_createsWithCountOne() {
        // Arrange
        when(userRepo.findById("seeker@test.com")).thenReturn(Optional.empty());
        when(userRepo.save(any(UserApplicationMetrics.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        analyticsService.incrementUserApplication("seeker@test.com");

        // Assert
        ArgumentCaptor<UserApplicationMetrics> captor = ArgumentCaptor.forClass(UserApplicationMetrics.class);
        verify(userRepo).save(captor.capture());
        assertEquals("seeker@test.com", captor.getValue().getUserEmail());
        assertEquals(1L, captor.getValue().getApplicationCount());
    }

    @Test
    void incrementUserApplication_existingUser_incrementsCount() {
        // Arrange
        UserApplicationMetrics existing = new UserApplicationMetrics("seeker@test.com", 2L);
        when(userRepo.findById("seeker@test.com")).thenReturn(Optional.of(existing));
        when(userRepo.save(any(UserApplicationMetrics.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        analyticsService.incrementUserApplication("seeker@test.com");

        // Assert
        ArgumentCaptor<UserApplicationMetrics> captor = ArgumentCaptor.forClass(UserApplicationMetrics.class);
        verify(userRepo).save(captor.capture());
        assertEquals(3L, captor.getValue().getApplicationCount());
    }

    // =============================================
    // getSummary tests
    // =============================================

    @Test
    void getSummary_returnsAllMetrics() {
        // Arrange
        List<MetricsSummary> metrics = List.of(
                new MetricsSummary(1L, "JOB_CREATED", 10L),
                new MetricsSummary(2L, "JOB_APPLIED", 25L),
                new MetricsSummary(3L, "RESUME_UPLOADED", 8L)
        );

        when(summaryRepo.findAll()).thenReturn(metrics);

        // Act
        Map<String, Long> summary = analyticsService.getSummary();

        // Assert
        assertNotNull(summary);
        assertEquals(3, summary.size());
        assertEquals(10L, summary.get("JOB_CREATED"));
        assertEquals(25L, summary.get("JOB_APPLIED"));
        assertEquals(8L, summary.get("RESUME_UPLOADED"));

        verify(summaryRepo).findAll();
    }

    @Test
    void getSummary_empty_returnsEmptyMap() {
        // Arrange
        when(summaryRepo.findAll()).thenReturn(List.of());

        // Act
        Map<String, Long> summary = analyticsService.getSummary();

        // Assert
        assertNotNull(summary);
        assertTrue(summary.isEmpty());
    }

    // =============================================
    // getJobMetrics / getUserMetrics tests
    // =============================================

    @Test
    void getJobMetrics_returnsPage() {
        org.springframework.data.domain.Page<JobApplicationMetrics> page =
                new org.springframework.data.domain.PageImpl<>(List.of(new JobApplicationMetrics(1L, 5L)));
        when(jobRepo.findAll(org.springframework.data.domain.PageRequest.of(0, 10))).thenReturn(page);

        org.springframework.data.domain.Page<JobApplicationMetrics> result = analyticsService.getJobMetrics(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getUserMetrics_returnsPage() {
        org.springframework.data.domain.Page<UserApplicationMetrics> page =
                new org.springframework.data.domain.PageImpl<>(List.of(new UserApplicationMetrics("seeker@test.com", 3L)));
        when(userRepo.findAll(org.springframework.data.domain.PageRequest.of(0, 10))).thenReturn(page);

        org.springframework.data.domain.Page<UserApplicationMetrics> result = analyticsService.getUserMetrics(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}

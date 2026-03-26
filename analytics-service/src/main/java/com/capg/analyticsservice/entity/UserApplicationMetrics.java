// Tracks applications per user (keyed by email since application-service stores email, not userId)
package com.capg.analyticsservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_application_metrics")
public class UserApplicationMetrics {

    @Id
    private String userEmail;

    private Long applicationCount;

    public UserApplicationMetrics() {}

    public UserApplicationMetrics(String userEmail, Long applicationCount) {
        this.userEmail = userEmail;
        this.applicationCount = applicationCount;
    }

    // Getters & Setters
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Long getApplicationCount() {
        return applicationCount;
    }

    public void setApplicationCount(Long applicationCount) {
        this.applicationCount = applicationCount;
    }
}
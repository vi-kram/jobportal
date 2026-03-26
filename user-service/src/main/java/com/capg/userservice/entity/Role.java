package com.capg.userservice.entity;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum Role {
    JOB_SEEKER,
    RECRUITER,
    ADMIN;

    @JsonCreator
    public static Role fromString(String value) {
        return Role.valueOf(value.toUpperCase());
    }
}
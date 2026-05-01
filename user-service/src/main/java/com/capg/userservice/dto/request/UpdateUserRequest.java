package com.capg.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateUserRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String mobile;
    private String skills;
    private String headline;

    public UpdateUserRequest() { /* default constructor for Jackson deserialization */ }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getHeadline() { return headline; }
    public void setHeadline(String headline) { this.headline = headline; }
}

package com.capg.userservice.controller;

import com.capg.userservice.config.SecurityConfig;
import com.capg.userservice.dto.request.UpdateUserRequest;
import com.capg.userservice.dto.request.UserLoginRequest;
import com.capg.userservice.dto.request.UserRegisterRequest;
import com.capg.userservice.dto.response.UserResponse;
import com.capg.userservice.entity.Role;
import com.capg.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(value = UserController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean private UserService userService;

    private UserResponse buildResponse() {
        return new UserResponse(1L, "John", "john@test.com", Role.JOB_SEEKER);
    }

    @Test
    @WithMockUser
    void registerUser_returns200() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("John", "john@test.com", "pass123", Role.JOB_SEEKER);
        when(userService.registerUser(any())).thenReturn(buildResponse());

        mockMvc.perform(post("/api/users/register").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser
    void loginUser_returns200() throws Exception {
        UserLoginRequest request = new UserLoginRequest("john@test.com", "pass123");
        when(userService.loginUser(any())).thenReturn("mocked.jwt.token");

        mockMvc.perform(post("/api/users/login").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("mocked.jwt.token"));
    }

    @Test
    @WithMockUser
    void getUser_returns200() throws Exception {
        when(userService.getUserById(anyLong(), anyString(), anyString())).thenReturn(buildResponse());

        mockMvc.perform(get("/api/users/1")
                .header("X-User-Email", "john@test.com")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void updateUser_returns200() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("Updated");
        when(userService.updateUser(anyLong(), any(), anyString(), anyString())).thenReturn(buildResponse());

        mockMvc.perform(put("/api/users/1").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .header("X-User-Email", "john@test.com")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getMe_returns200() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(buildResponse());

        mockMvc.perform(get("/api/users/me")
                .header("X-User-Email", "john@test.com"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "RECRUITER")
    void getUserByEmail_asRecruiter_returns200() throws Exception {
        when(userService.getUserByEmail(anyString())).thenReturn(buildResponse());

        mockMvc.perform(get("/api/users/by-email/john@test.com")
                .header("X-User-Role", "RECRUITER"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getUserByEmail_asJobSeeker_returns403() throws Exception {
        mockMvc.perform(get("/api/users/by-email/john@test.com")
                .header("X-User-Role", "JOB_SEEKER"))
                .andExpect(status().isForbidden());
    }
}

package com.capg.userservice.dto;

import com.capg.userservice.dto.request.UpdateUserRequest;
import com.capg.userservice.dto.request.UserLoginRequest;
import com.capg.userservice.dto.request.UserRegisterRequest;
import com.capg.userservice.dto.response.UserResponse;
import com.capg.userservice.entity.Role;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DtoTest {

    @Test
    void userRegisterRequest_gettersSettersWork() {
        UserRegisterRequest req = new UserRegisterRequest("John", "john@test.com", "pass123", Role.JOB_SEEKER);
        assertEquals("John", req.getName());
        assertEquals("john@test.com", req.getEmail());
        assertEquals("pass123", req.getPassword());
        assertEquals(Role.JOB_SEEKER, req.getRole());

        req.setName("Jane");
        req.setEmail("jane@test.com");
        req.setPassword("newpass");
        req.setRole(Role.RECRUITER);
        assertEquals("Jane", req.getName());
        assertEquals(Role.RECRUITER, req.getRole());
    }

    @Test
    void userLoginRequest_gettersSettersWork() {
        UserLoginRequest req = new UserLoginRequest("john@test.com", "pass123");
        assertEquals("john@test.com", req.getEmail());
        assertEquals("pass123", req.getPassword());

        req.setEmail("jane@test.com");
        req.setPassword("newpass");
        assertEquals("jane@test.com", req.getEmail());
        assertEquals("newpass", req.getPassword());
    }

    @Test
    void userResponse_gettersSettersWork() {
        UserResponse res = new UserResponse(1L, "John", "john@test.com", Role.JOB_SEEKER);
        assertEquals(1L, res.getId());
        assertEquals("John", res.getName());
        assertEquals("john@test.com", res.getEmail());
        assertEquals(Role.JOB_SEEKER, res.getRole());

        res.setId(2L);
        res.setName("Jane");
        res.setEmail("jane@test.com");
        res.setRole(Role.RECRUITER);
        assertEquals(2L, res.getId());
        assertEquals("Jane", res.getName());
    }

    @Test
    void updateUserRequest_gettersSettersWork() {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setName("Updated");
        req.setPassword("newpass");
        assertEquals("Updated", req.getName());
        assertEquals("newpass", req.getPassword());
    }

    @Test
    void userResponse_noArgConstructor_works() {
        UserResponse res = new UserResponse();
        assertNotNull(res);
        res.setId(1L);
        res.setName("John");
        res.setEmail("john@test.com");
        res.setRole(Role.JOB_SEEKER);
        assertEquals(1L, res.getId());
        assertEquals("John", res.getName());
    }
}

package com.capg.userservice.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void user_allGettersSettersWork() {
        LocalDateTime now = LocalDateTime.now();
        User user = new User(1L, "John", "john@test.com", "pass", Role.JOB_SEEKER, true, now, now);

        assertEquals(1L, user.getId());
        assertEquals("John", user.getName());
        assertEquals("john@test.com", user.getEmail());
        assertEquals("pass", user.getPassword());
        assertEquals(Role.JOB_SEEKER, user.getRole());
        assertTrue(user.isActive());
        assertEquals(now, user.getCreatedAt());
        assertEquals(now, user.getUpdatedAt());

        user.setId(2L);
        user.setName("Jane");
        user.setEmail("jane@test.com");
        user.setPassword("newpass");
        user.setRole(Role.RECRUITER);
        user.setActive(false);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        assertEquals(2L, user.getId());
        assertEquals("Jane", user.getName());
        assertEquals(Role.RECRUITER, user.getRole());
        assertFalse(user.isActive());
    }

    @Test
    void user_noArgConstructor_works() {
        User user = new User();
        assertNotNull(user);
        user.setId(1L);
        user.setName("Test");
        assertEquals(1L, user.getId());
        assertEquals("Test", user.getName());
    }
}

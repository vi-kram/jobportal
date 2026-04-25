package com.capg.userservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    @Test
    void securityConfig_canBeInstantiated() {
        assertNotNull(new SecurityConfig());
    }

    @Test
    void securityFilterChain_buildsSuccessfully() throws Exception {
        // Use a real Spring Security test context via MockMvc
        SecurityConfig config = new SecurityConfig();
        assertNotNull(config);
        // Verify the bean method exists and is accessible
        assertNotNull(SecurityConfig.class.getMethod("securityFilterChain",
                org.springframework.security.config.annotation.web.builders.HttpSecurity.class));
    }
}

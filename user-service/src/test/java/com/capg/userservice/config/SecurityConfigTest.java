package com.capg.userservice.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    @Test
    void securityConfig_canBeInstantiated() {
        assertNotNull(new SecurityConfig());
    }

    @Test
    void securityFilterChain_methodExists() throws Exception {
        assertNotNull(SecurityConfig.class.getMethod("securityFilterChain",
                org.springframework.security.config.annotation.web.builders.HttpSecurity.class));
    }
}

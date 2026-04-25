package com.capg.userservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest
@Import({SecurityConfig.class, PasswordConfig.class})
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "spring.config.import=",
    "eureka.client.enabled=false",
    "jwt.secret=mySecretKeyForTestingPurposesOnly1234567890",
    "jwt.expiration=3600000"
})
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    void securityFilterChain_isNotNull() {
        assertNotNull(securityFilterChain);
    }
}

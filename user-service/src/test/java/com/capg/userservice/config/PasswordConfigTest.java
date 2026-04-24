package com.capg.userservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;

class PasswordConfigTest {

    @Test
    void passwordEncoder_returnsWorkingEncoder() {
        PasswordConfig config = new PasswordConfig();
        PasswordEncoder encoder = config.passwordEncoder();
        assertNotNull(encoder);
        String encoded = encoder.encode("password");
        assertTrue(encoder.matches("password", encoded));
    }
}

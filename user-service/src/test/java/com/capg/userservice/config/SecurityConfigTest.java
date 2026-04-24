package com.capg.userservice.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.web.SecurityFilterChain;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    classes = {SecurityConfig.class, PasswordConfig.class},
    webEnvironment = WebEnvironment.MOCK,
    properties = {
        "spring.cloud.config.enabled=false",
        "spring.config.import=",
        "eureka.client.enabled=false",
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration,org.springframework.cloud.config.client.ConfigClientAutoConfiguration",
        "jwt.secret=mySecretKeyForTestingPurposesOnly1234567890",
        "jwt.expiration=3600000"
    }
)
class SecurityConfigTest {

    @Autowired
    private SecurityFilterChain securityFilterChain;

    @Test
    void securityFilterChain_isNotNull() {
        assertNotNull(securityFilterChain);
    }
}

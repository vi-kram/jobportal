package com.capg.applicationservice.client;

import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    @Override
    public Object getUserById(Long id) {
        throw new RuntimeException("User Service is currently unavailable.");
    }
}

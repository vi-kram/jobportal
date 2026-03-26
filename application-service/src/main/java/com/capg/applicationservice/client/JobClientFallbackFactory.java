package com.capg.applicationservice.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import com.capg.applicationservice.exception.ResourceNotFoundException;

@Component
public class JobClientFallbackFactory implements FallbackFactory<JobClient> {

    @Override
    public JobClient create(Throwable cause) {
        return new JobClient() {

            @Override
            public Object getJobById(Long id) {
                throw new ResourceNotFoundException(
                        "Job Service is currently unavailable (Circuit Breaker)"
                );
            }
        };
    }
}

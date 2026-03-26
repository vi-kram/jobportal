package com.capg.applicationservice.client;

import org.springframework.stereotype.Component;

@Component
public class JobClientFallback implements JobClient {

	@Override
	public Object getJobById(Long id) {
        throw new RuntimeException("Job Service is currently unavailable. Please try again later.");
	}
}

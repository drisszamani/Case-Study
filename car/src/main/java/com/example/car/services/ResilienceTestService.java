package com.example.car.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class ResilienceTestService {
    private final RestTemplate restTemplate;
    private final WebClient webClient;
    private final String BASE_URL = "http://localhost:8888/SERVICE-CLIENT/api/client";

    public String testResilience() {
        StringBuilder report = new StringBuilder();

        // Test service availability
        try {
            restTemplate.getForEntity(BASE_URL + "/health", String.class);
            report.append("Service Client is available\n");
        } catch (Exception e) {
            report.append("Service Client is not available: ").append(e.getMessage()).append("\n");
        }

        // Test error handling
        try {
            restTemplate.getForEntity(BASE_URL + "/invalid-endpoint", String.class);
        } catch (Exception e) {
            report.append("Error handling working as expected\n");
        }

        return report.toString();
    }
}
package com.example.car.services;

import com.example.car.clients.ClientRestClient;
import com.example.car.entities.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ComparisonTestService {
    private final RestTemplate restTemplate;
    private final ClientRestClient feignClient;
    private final WebClient webClient;
    private final String BASE_URL = "http://localhost:8888/SERVICE-CLIENT/api/client";

    public Map<String, Object> compareAllMethods() {
        Map<String, Object> results = new HashMap<>();

        // Test RestTemplate
        long startTime = System.currentTimeMillis();
        results.put("RestTemplate_Response", restTemplate.getForObject(BASE_URL, Client[].class));
        results.put("RestTemplate_Time", System.currentTimeMillis() - startTime + "ms");

        // Test FeignClient
        startTime = System.currentTimeMillis();
        results.put("FeignClient_Response", feignClient.findAll());
        results.put("FeignClient_Time", System.currentTimeMillis() - startTime + "ms");

        // Test WebClient
        startTime = System.currentTimeMillis();
        results.put("WebClient_Response", webClient.get()
                .uri("/api/client")
                .retrieve()
                .bodyToMono(Client[].class)
                .block());
        results.put("WebClient_Time", System.currentTimeMillis() - startTime + "ms");

        return results;
    }
}
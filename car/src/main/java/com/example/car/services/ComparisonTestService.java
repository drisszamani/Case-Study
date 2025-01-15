package com.example.car.services;

import com.example.car.clients.ClientRestClient;
import com.example.car.entities.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

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
        Map<String, Object> comparison = new HashMap<>();

        // Measure RestTemplate performance
        long startTime = System.currentTimeMillis();
        restTemplate.getForObject(BASE_URL, Client[].class);
        long restTemplateTime = System.currentTimeMillis() - startTime;
        comparison.put("RestTemplate_Time", restTemplateTime);

        // Measure FeignClient performance
        startTime = System.currentTimeMillis();
        feignClient.findAll();
        long feignTime = System.currentTimeMillis() - startTime;
        comparison.put("FeignClient_Time", feignTime);

        // Measure WebClient performance
        startTime = System.currentTimeMillis();
        webClient.get()
                .uri("/api/client")
                .retrieve()
                .bodyToMono(Client[].class)
                .block();
        long webClientTime = System.currentTimeMillis() - startTime;
        comparison.put("WebClient_Time", webClientTime);

        return comparison;
    }
}
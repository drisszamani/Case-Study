package com.example.car.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ResilienceTestService {
    @Autowired
    private RestTemplate restTemplate;
    private final String URL = "http://localhost:8888/SERVICE-CLIENT";

    public String testResilience() {
        try {
            restTemplate.getForEntity(URL + "/api/client", String.class);
            return "Service Client accessible";
        } catch (Exception e) {
            return "Service Client inaccessible: " + e.getMessage();
        }
    }
}
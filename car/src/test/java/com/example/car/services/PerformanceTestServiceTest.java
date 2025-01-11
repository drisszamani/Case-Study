package com.example.car.services;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class PerformanceTestServiceTest {

    @Autowired
    private PerformanceTestService performanceTestService;

    @Test
    void testPerformanceMetrics() {
        Map<String, Object> results = performanceTestService.runPerformanceTests();

        assertNotNull(results);

        // Test Response Time and Throughput
        assertTrue(results.containsKey("RestTemplate_ResponseTime"));
        assertTrue(results.containsKey("FeignClient_ResponseTime"));
        assertTrue(results.containsKey("WebClient_ResponseTime"));

        // Test Resource Consumption
        assertTrue(results.containsKey("RestTemplate_CPU"));
        assertTrue(results.containsKey("FeignClient_CPU"));
        assertTrue(results.containsKey("WebClient_CPU"));

        // Test Energy Consumption
        assertTrue(results.containsKey("RestTemplate_Energy"));
        assertTrue(results.containsKey("FeignClient_Energy"));
        assertTrue(results.containsKey("WebClient_Energy"));
    }
}
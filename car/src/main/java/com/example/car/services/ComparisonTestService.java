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

    public Map<String, Map<String, Object>> compareAllMethods() {
        Map<String, Map<String, Object>> results = new HashMap<>();
        results.put("RestTemplate", testRestTemplate());
        results.put("FeignClient", testFeignClient());
        results.put("WebClient", testWebClient());
        return results;
    }

    private Map<String, Object> testRestTemplate() {
        Map<String, Object> metrics = new HashMap<>();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        long startMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long startTime = System.currentTimeMillis();
        long startCpu = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();

        restTemplate.getForObject(BASE_URL, Client[].class);

        metrics.put("responseTime", System.currentTimeMillis() - startTime + "ms");
        metrics.put("memoryUsed", (memoryBean.getHeapMemoryUsage().getUsed() - startMemory) / 1024 + "KB");
        metrics.put("cpuTime", (ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - startCpu) / 1_000_000 + "ms");

        return metrics;
    }

    private Map<String, Object> testFeignClient() {
        Map<String, Object> metrics = new HashMap<>();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        long startMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long startTime = System.currentTimeMillis();
        long startCpu = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();

        feignClient.findAll();

        metrics.put("responseTime", System.currentTimeMillis() - startTime + "ms");
        metrics.put("memoryUsed", (memoryBean.getHeapMemoryUsage().getUsed() - startMemory) / 1024 + "KB");
        metrics.put("cpuTime", (ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - startCpu) / 1_000_000 + "ms");

        return metrics;
    }

    private Map<String, Object> testWebClient() {
        Map<String, Object> metrics = new HashMap<>();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        long startMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long startTime = System.currentTimeMillis();
        long startCpu = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();

        webClient.get()
                .uri("/api/client")
                .retrieve()
                .bodyToMono(Client[].class)
                .block();

        metrics.put("responseTime", System.currentTimeMillis() - startTime + "ms");
        metrics.put("memoryUsed", (memoryBean.getHeapMemoryUsage().getUsed() - startMemory) / 1024 + "KB");
        metrics.put("cpuTime", (ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - startCpu) / 1_000_000 + "ms");

        return metrics;
    }
}
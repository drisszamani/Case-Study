package com.example.car.services;

import com.example.car.clients.ClientRestClient;
import com.example.car.entities.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class PerformanceTestService {
    private final RestTemplate restTemplate;
    private final ClientRestClient feignClient;
    private final WebClient webClient;
    private final String BASE_URL = "http://localhost:8888/SERVICE-CLIENT/api/client";

    public Map<String, Object> runPerformanceTests() {
        Map<String, Object> results = new HashMap<>();

        // 4.12.1 Performance (Response Time and Throughput)
        results.putAll(testResponseTimeAndThroughput());

        // 4.12.2 Resource Consumption
        results.putAll(testResourceConsumption());

        // 4.12.3 Energy Consumption
        results.putAll(testEnergyConsumption());

        return results;
    }

    private Map<String, Object> testResponseTimeAndThroughput() {
        Map<String, Object> metrics = new HashMap<>();
        int numberOfRequests = 1000; // As per documentation: 10 to 1000 requests

        // Test RestTemplate
        TestResult restTemplateResult = performLoadTest(numberOfRequests, () ->
                restTemplate.getForObject(BASE_URL, Client[].class));
        metrics.put("RestTemplate_ResponseTime", restTemplateResult.avgResponseTime);
        metrics.put("RestTemplate_Throughput", restTemplateResult.throughput);

        // Test Feign Client
        TestResult feignResult = performLoadTest(numberOfRequests, () ->
                feignClient.findAll());
        metrics.put("FeignClient_ResponseTime", feignResult.avgResponseTime);
        metrics.put("FeignClient_Throughput", feignResult.throughput);

        // Test WebClient
        TestResult webClientResult = performLoadTest(numberOfRequests, () ->
                webClient.get()
                        .uri("/api/client")
                        .retrieve()
                        .bodyToMono(Client[].class)
                        .block());
        metrics.put("WebClient_ResponseTime", webClientResult.avgResponseTime);
        metrics.put("WebClient_Throughput", webClientResult.throughput);

        return metrics;
    }

    private Map<String, Object> testResourceConsumption() {
        Map<String, Object> metrics = new HashMap<>();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();

        // Test RestTemplate
        ResourceMetrics restTemplateMetrics = measureResourceUsage(() ->
                restTemplate.getForObject(BASE_URL, Client[].class));
        metrics.put("RestTemplate_CPU", restTemplateMetrics.cpuUsage);
        metrics.put("RestTemplate_Memory", restTemplateMetrics.memoryUsage);

        // Test Feign Client
        ResourceMetrics feignMetrics = measureResourceUsage(() ->
                feignClient.findAll());
        metrics.put("FeignClient_CPU", feignMetrics.cpuUsage);
        metrics.put("FeignClient_Memory", feignMetrics.memoryUsage);

        // Test WebClient
        ResourceMetrics webClientMetrics = measureResourceUsage(() ->
                webClient.get()
                        .uri("/api/client")
                        .retrieve()
                        .bodyToMono(Client[].class)
                        .block());
        metrics.put("WebClient_CPU", webClientMetrics.cpuUsage);
        metrics.put("WebClient_Memory", webClientMetrics.memoryUsage);

        return metrics;
    }

    private Map<String, Object> testEnergyConsumption() {
        Map<String, Object> metrics = new HashMap<>();
        int requestsPerBatch = 1000;

        // Estimate energy consumption based on CPU utilization and duration
        metrics.put("RestTemplate_Energy", measureEnergyConsumption(() ->
                restTemplate.getForObject(BASE_URL, Client[].class), requestsPerBatch));

        metrics.put("FeignClient_Energy", measureEnergyConsumption(() ->
                feignClient.findAll(), requestsPerBatch));

        metrics.put("WebClient_Energy", measureEnergyConsumption(() ->
                webClient.get()
                        .uri("/api/client")
                        .retrieve()
                        .bodyToMono(Client[].class)
                        .block(), requestsPerBatch));

        return metrics;
    }

    private TestResult performLoadTest(int numberOfRequests, Runnable operation) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        AtomicInteger successfulRequests = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfRequests; i++) {
            executor.submit(() -> {
                try {
                    long requestStart = System.currentTimeMillis();
                    operation.run();
                    successfulRequests.incrementAndGet();
                } catch (Exception e) {
                    // Log error
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long duration = System.currentTimeMillis() - startTime;
        double avgResponseTime = duration / (double) successfulRequests.get();
        double throughput = (successfulRequests.get() / (duration / 1000.0));

        return new TestResult(avgResponseTime, throughput);
    }

    private ResourceMetrics measureResourceUsage(Runnable operation) {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long startMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long startCpuTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();

        operation.run();

        long endMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long endCpuTime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();

        double cpuUsage = (endCpuTime - startCpuTime) / 1_000_000.0; // Convert to ms
        double memoryUsage = (endMemory - startMemory) / (1024.0 * 1024.0); // Convert to MB

        return new ResourceMetrics(cpuUsage, memoryUsage);
    }

    private double measureEnergyConsumption(Runnable operation, int requests) {
        // Simplified energy consumption estimation based on CPU usage and time
        ResourceMetrics metrics = measureResourceUsage(() -> {
            for (int i = 0; i < requests; i++) {
                operation.run();
            }
        });

        // Rough estimation: Energy (Wh) = Power (W) * Time (h)
        // Assuming average CPU power consumption of 50W at full load
        return (metrics.cpuUsage / 100.0) * 50.0 * (metrics.cpuUsage / 3600000.0);
    }

    private static class TestResult {
        final double avgResponseTime;
        final double throughput;

        TestResult(double avgResponseTime, double throughput) {
            this.avgResponseTime = avgResponseTime;
            this.throughput = throughput;
        }
    }

    private static class ResourceMetrics {
        final double cpuUsage;
        final double memoryUsage;

        ResourceMetrics(double cpuUsage, double memoryUsage) {
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
        }
    }
}
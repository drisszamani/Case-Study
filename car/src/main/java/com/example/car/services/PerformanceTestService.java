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

        results.putAll(testResponseTimeAndThroughput());
        results.putAll(testResourceConsumption());
        results.putAll(testEnergyConsumption());

        return results;
    }

    private Map<String, Object> testResponseTimeAndThroughput() {
        Map<String, Object> metrics = new HashMap<>();
        int numberOfRequests = 1000;

        TestResult restTemplateResult = performLoadTest(numberOfRequests, () ->
                restTemplate.getForObject(BASE_URL, Client[].class));
        metrics.put("RestTemplate_ResponseTime", restTemplateResult.avgResponseTime);
        metrics.put("RestTemplate_Throughput", restTemplateResult.throughput);

        TestResult feignResult = performLoadTest(numberOfRequests, () ->
                feignClient.findAll());
        metrics.put("FeignClient_ResponseTime", feignResult.avgResponseTime);
        metrics.put("FeignClient_Throughput", feignResult.throughput);

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

        ResourceMetrics restTemplateMetrics = measureResourceUsage(() ->
                restTemplate.getForObject(BASE_URL, Client[].class));
        metrics.put("RestTemplate_CPU", restTemplateMetrics.cpuUsage);
        metrics.put("RestTemplate_Memory", restTemplateMetrics.memoryUsage);

        ResourceMetrics feignMetrics = measureResourceUsage(() ->
                feignClient.findAll());
        metrics.put("FeignClient_CPU", feignMetrics.cpuUsage);
        metrics.put("FeignClient_Memory", feignMetrics.memoryUsage);

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
                    operation.run();
                    successfulRequests.incrementAndGet();
                } catch (Exception ignored) {
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

        double cpuUsage = (endCpuTime - startCpuTime) / 1_000_000.0;
        double memoryUsage = (endMemory - startMemory) / (1024.0 * 1024.0);

        return new ResourceMetrics(cpuUsage, memoryUsage);
    }

    private double measureEnergyConsumption(Runnable operation, int requests) {
        ResourceMetrics metrics = measureResourceUsage(() -> {
            for (int i = 0; i < requests; i++) {
                operation.run();
            }
        });

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

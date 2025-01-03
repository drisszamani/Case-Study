package com.example.car.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.car.models.CarResponse;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Service
public class PerformanceTestService {
    @Autowired
    private CarService carService;

    public Map<String, Object> testSynchronousClient() {
        Map<String, Object> results = new HashMap<>();

        // Test de temps de réponse
        results.putAll(measureResponseTime());

        // Test de débit
        results.putAll(measureThroughput());

        // Test de ressources
        results.putAll(measureResourceConsumption());

        return results;
    }

    private Map<String, Object> measureResponseTime() {
        Map<String, Object> metrics = new HashMap<>();

        long startTime = System.currentTimeMillis();
        List<CarResponse> cars = carService.findAll();
        long endTime = System.currentTimeMillis();

        metrics.put("tempsReponseTotal", (endTime - startTime) + " ms");
        metrics.put("tempsReponseMoyen", (endTime - startTime) / (double)cars.size() + " ms");

        return metrics;
    }

    private Map<String, Object> measureThroughput() {
        Map<String, Object> metrics = new HashMap<>();
        int numberOfRequests = 100;
        ExecutorService executor = Executors.newFixedThreadPool(10);

        long startTime = System.currentTimeMillis();
        List<Future<?>> futures = new ArrayList<>();

        for (int i = 0; i < numberOfRequests; i++) {
            futures.add(executor.submit(() -> {
                try {
                    carService.findAll();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
        }

        futures.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        long endTime = System.currentTimeMillis();
        double duration = (endTime - startTime) / 1000.0; // en secondes

        metrics.put("debit", numberOfRequests / duration + " requêtes/seconde");

        executor.shutdown();
        return metrics;
    }

    private Map<String, Object> measureResourceConsumption() {
        Map<String, Object> metrics = new HashMap<>();

        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        long startCpuTime = threadBean.getCurrentThreadCpuTime();
        long startMemory = memoryBean.getHeapMemoryUsage().getUsed();

        carService.findAll();

        long endCpuTime = threadBean.getCurrentThreadCpuTime();
        long endMemory = memoryBean.getHeapMemoryUsage().getUsed();

        metrics.put("cpuUsage", (endCpuTime - startCpuTime) / 1_000_000 + " ms");
        metrics.put("memoireUtilisee", (endMemory - startMemory) / (1024 * 1024) + " MB");

        return metrics;
    }
}
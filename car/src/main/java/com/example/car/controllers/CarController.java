package com.example.car.controllers;

import com.example.car.models.CarResponse;
import com.example.car.services.CarService;
import com.example.car.services.PerformanceTestService;
import com.example.car.services.ResilienceTestService;
import com.example.car.services.ComparisonTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/car")
public class CarController {

    @Autowired
    private CarService carService;

    @Autowired
    private PerformanceTestService performanceTestService;

    @Autowired
    private ResilienceTestService resilienceTestService;

    @Autowired
    private ComparisonTestService comparisonTestService; // Injection manquante ajoutée

    @GetMapping
    public List<CarResponse> findAll() {
        return carService.findAll();
    }

    @GetMapping("/{id}")
    public CarResponse findById(@PathVariable Long id) throws Exception {
        return carService.findById(id);
    }

    @GetMapping("/test-performance")
    public Map<String, Object> testPerformance() {
        return performanceTestService.testSynchronousClient();
    }

    @GetMapping("/test-resilience")
    public String testResilience() {
        return resilienceTestService.testResilience();
    }

    @GetMapping("/compare")
    public Map<String, Map<String, Object>> compareAllMethods() {
        return comparisonTestService.compareAllMethods();
    }
}

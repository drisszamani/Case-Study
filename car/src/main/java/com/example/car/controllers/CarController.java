package com.example.car.controllers;

import com.example.car.models.CarResponse;
import com.example.car.services.CarService;
import com.example.car.services.ComparisonTestService;
import com.example.car.services.PerformanceTestService;
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
    private ComparisonTestService comparisonTestService;

    @Autowired
    private PerformanceTestService performanceTestService;  // Added this line

    @GetMapping
    public List findAll() {
        return carService.findAll();
    }

    @GetMapping("/{id}")
    public CarResponse findById(@PathVariable Long id) throws Exception {
        return carService.findById(id);
    }

    @GetMapping("/compare")
    public Map compareClients() {
        return comparisonTestService.compareAllMethods();
    }

    @GetMapping("/performance")
    public Map runPerformanceTests() {
        return performanceTestService.runPerformanceTests();
    }
}
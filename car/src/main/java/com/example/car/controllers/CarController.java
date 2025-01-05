package com.example.car.controllers;

import com.example.car.models.CarResponse;
import com.example.car.services.CarService;
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
    private ComparisonTestService comparisonTestService;

    @GetMapping
    public List<CarResponse> findAll() {
        return carService.findAll();
    }

    @GetMapping("/{id}")
    public CarResponse findById(@PathVariable Long id) throws Exception {
        return carService.findById(id);
    }

    @GetMapping("/compare")
    public Map<String, Object> compareClients() {
        return comparisonTestService.compareAllMethods();
    }
}
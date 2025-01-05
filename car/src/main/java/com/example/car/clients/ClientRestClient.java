package com.example.car.clients;

import com.example.car.entities.Client;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "SERVICE-CLIENT", url = "http://localhost:8888/SERVICE-CLIENT")
public interface ClientRestClient {
    @GetMapping("/api/client")
    List<Client> findAll();

    @GetMapping("/api/client/{id}")
    Client findById(@PathVariable Long id);
}
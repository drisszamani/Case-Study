package com.example.car.services;

import com.example.car.clients.ClientRestClient;
import com.example.car.entities.Client;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.config.enabled=false"
})
@ActiveProfiles("test")
class PerformanceAndResilienceTests {
    @MockBean
    private RestTemplate restTemplate;

    @MockBean
    private ClientRestClient feignClient;

    @MockBean
    private WebClient webClient;

    @Autowired
    private PerformanceTestService performanceTestService;

    @Autowired
    private ResilienceTestService resilienceTestService;

    @Autowired
    private ComparisonTestService comparisonTestService;

    @MockBean
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @MockBean
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @MockBean
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setup() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(any(String.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Client[].class)).thenReturn(Mono.just(new Client[]{new Client(1L, "Test Client", 25)}));
    }

    @Test
    void performanceTestShouldExecuteWithoutError() {
        Client[] mockClientsArray = new Client[]{new Client(1L, "Test Client", 25)};
        List<Client> mockClientsList = Arrays.asList(mockClientsArray);

        when(restTemplate.getForObject(any(String.class), eq(Client[].class)))
                .thenReturn(mockClientsArray);
        when(feignClient.findAll())
                .thenReturn(mockClientsList);

        Map<String, Object> results = performanceTestService.runPerformanceTests();

        assertNotNull(results);
        assertTrue(results.containsKey("RestTemplate_ResponseTime"));
        assertTrue(results.containsKey("FeignClient_ResponseTime"));
        assertTrue(results.containsKey("WebClient_ResponseTime"));
        assertTrue((Double) results.get("RestTemplate_ResponseTime") >= 0);
        assertTrue((Double) results.get("FeignClient_ResponseTime") >= 0);
    }

    @Test
    void resilienceTestShouldHandleErrors() {
        when(restTemplate.getForEntity(any(String.class), eq(String.class)))
                .thenReturn(null);

        String result = resilienceTestService.testResilience();

        assertNotNull(result);
        assertTrue(result.contains("Service Client"));
    }

    @Test
    void comparisonTestShouldExecuteWithoutError() {
        Client[] mockClientsArray = new Client[]{new Client(1L, "Test Client", 25)};
        List<Client> mockClientsList = Arrays.asList(mockClientsArray);

        when(restTemplate.getForObject(any(String.class), eq(Client[].class)))
                .thenReturn(mockClientsArray);
        when(feignClient.findAll())
                .thenReturn(mockClientsList);

        Map<String, Object> results = comparisonTestService.compareAllMethods();

        assertNotNull(results);
        assertTrue(results.containsKey("RestTemplate_Time"));
        assertTrue(results.containsKey("FeignClient_Time"));
        assertTrue(results.containsKey("WebClient_Time"));
    }
}
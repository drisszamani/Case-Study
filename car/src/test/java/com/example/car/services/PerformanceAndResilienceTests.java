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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest
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

    @BeforeEach
    void setup() {
        // Mock WebClient chain
        WebClient.RequestHeadersUriSpec requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = mock(WebClient.ResponseSpec.class);

        // Setup the WebClient mock chain
        lenient().when(webClient.get()).thenReturn((WebClient.RequestHeadersUriSpec) requestHeadersUriSpec);
        lenient().when(requestHeadersUriSpec.uri(anyString())).thenReturn((WebClient.RequestHeadersSpec) requestHeadersSpec);
        lenient().when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // Mock the response
        Client[] mockClientsArray = new Client[]{new Client(1L, "Test Client", 25)};
        Mono<Client[]> mockResponse = Mono.just(mockClientsArray);
        lenient().when(responseSpec.bodyToMono(Client[].class)).thenReturn(mockResponse);
    }

    @Test
    void performanceTestShouldExecuteWithoutError() {
        // Prepare test data
        Client[] mockClientsArray = new Client[]{new Client(1L, "Test Client", 25)};
        List<Client> mockClientsList = Arrays.asList(mockClientsArray);

        // Configure mocks
        when(restTemplate.getForObject(anyString(), eq(Client[].class)))
                .thenReturn(mockClientsArray);
        when(feignClient.findAll())
                .thenReturn(mockClientsList);

        // Execute test
        Map<String, Object> results = performanceTestService.runPerformanceTests();

        // Verify results
        assertNotNull(results);
        assertTrue(results.containsKey("RestTemplate_ResponseTime"));
        assertTrue(results.containsKey("FeignClient_ResponseTime"));
        assertTrue(results.containsKey("WebClient_ResponseTime"));
        assertTrue((Double) results.get("RestTemplate_ResponseTime") >= 0);
        assertTrue((Double) results.get("FeignClient_ResponseTime") >= 0);
        assertTrue((Double) results.get("WebClient_ResponseTime") >= 0);
    }

    @Test
    void resilienceTestShouldHandleErrors() {
        when(restTemplate.getForEntity(anyString(), eq(String.class)))
                .thenReturn(null);

        String result = resilienceTestService.testResilience();

        assertNotNull(result);
        assertTrue(result.contains("Service Client"));
    }

    @Test
    void comparisonTestShouldExecuteWithoutError() {
        // Prepare test data
        Client[] mockClientsArray = new Client[]{new Client(1L, "Test Client", 25)};
        List<Client> mockClientsList = Arrays.asList(mockClientsArray);

        // Configure mocks
        when(restTemplate.getForObject(anyString(), eq(Client[].class)))
                .thenReturn(mockClientsArray);
        when(feignClient.findAll())
                .thenReturn(mockClientsList);

        // Execute test
        Map<String, Object> results = comparisonTestService.compareAllMethods();

        // Verify results
        assertNotNull(results);
        assertTrue(results.containsKey("RestTemplate_Time"));
        assertTrue(results.containsKey("FeignClient_Time"));
        assertTrue(results.containsKey("WebClient_Time"));
    }
}
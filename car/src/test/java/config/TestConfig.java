package config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.example.car.clients.ClientRestClient;

@TestConfiguration
public class TestConfig {
    @MockBean
    private ClientRestClient clientRestClient;

    @Bean
    @Primary
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @Primary
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
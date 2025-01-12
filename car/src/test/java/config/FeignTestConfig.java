package config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import com.example.car.clients.ClientRestClient;
import org.mockito.Mockito;

@TestConfiguration
public class FeignTestConfig {

    @Bean
    @Primary
    public ClientRestClient clientRestClient() {
        return Mockito.mock(ClientRestClient.class);
    }
}
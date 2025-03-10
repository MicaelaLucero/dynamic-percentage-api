package com.tenpo.challenge.client;

import com.tenpo.challenge.client.impl.ExternalApiClientImpl;
import com.tenpo.challenge.config.properties.ExternalApiProperties;
import com.tenpo.challenge.config.retry.RetryConfig;
import com.tenpo.challenge.exception.ExternalApiException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@TestPropertySource(properties = {
        "retry.max-attempts=3",
        "retry.delay=2000"
})
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ExternalApiClientRetryableTest.Config.class)
class ExternalApiClientRetryableTest {

    @Autowired
    private ExternalApiClient externalApiClient;

    @Test
    void shouldRetryOnFailureAndFailAfterMaxAttempts() {
        log.info("Testing retry mechanism...");

        ExternalApiException exception = assertThrows(ExternalApiException.class, () -> externalApiClient.getPercentage());

        assertTrue(exception.getMessage().contains("API unavailable"));
    }


    @ExtendWith(OutputCaptureExtension.class)
    @Test
    void shouldRetryOnFailureAndFailAfterMaxAttempts(CapturedOutput output) {
        ExternalApiException exception = assertThrows(ExternalApiException.class, () -> externalApiClient.getPercentage());

        assertTrue(exception.getMessage().contains("API unavailable"));

        long attemptCount = output.getOut().lines()
                .filter(line -> line.contains("Attempting to fetch percentage..."))
                .count();

        assertEquals(3, attemptCount, "There should be exactly 3 retry attempts before failing");
    }

    @TestConfiguration
    @Import(RetryConfig.class)
    static class Config {

        private static final String BASE_URL = "http://localhost:8085";

        @Bean
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }

        @Bean
        public ExternalApiClientImpl externalApiClient(RestTemplate restTemplate) {
            return new ExternalApiClientImpl(
                    restTemplate, new ExternalApiProperties(
                            BASE_URL + "/external-percentage",
                    BASE_URL + "/__admin"))
            {
                @Override
                public Double getPercentage() {
                    log.info("Attempting to fetch percentage...");
                    throw new ExternalApiException("API unavailable");
                }
            };
        }
    }
}
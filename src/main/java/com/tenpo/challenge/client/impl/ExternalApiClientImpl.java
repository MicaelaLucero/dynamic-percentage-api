package com.tenpo.challenge.client.impl;

import com.tenpo.challenge.client.ExternalApiClient;
import com.tenpo.challenge.dto.UpdatePercentageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static com.tenpo.challenge.utils.Constants.EXTERNAL_API_URL;
import static com.tenpo.challenge.utils.Constants.WIREMOCK_ADMIN_URL;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Slf4j
@Component
public class ExternalApiClientImpl implements ExternalApiClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Retryable(interceptor = "retryInterceptor")
    public Double getPercentage() {
        try {
            log.info("Calling external API: {}", EXTERNAL_API_URL);
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    EXTERNAL_API_URL,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            if (response.getBody() != null && response.getBody().containsKey("percentage")) {
                Object value = response.getBody().get("percentage");
                log.info("External API response: {}", value);

                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                }
            }

            log.error("Invalid response from external API: {}", response.getBody());
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Invalid response from external API");

        } catch (Exception e) {
            log.error("Error connecting to external API", e);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "External API unavailable", e);
        }
    }

    @Override
    public UpdatePercentageResponse updatePercentage(Double newPercentage) {
        resetWireMock();

        HttpEntity<String> request = createMockRequest(newPercentage);

        return updateMockRequest(newPercentage, request);
    }

    private void resetWireMock() {
        try {
            restTemplate.exchange(
                    WIREMOCK_ADMIN_URL + "/mappings/reset",
                    HttpMethod.POST,
                    null,
                    String.class
            );
            log.info("WireMock mappings reset successfully.");
        } catch (Exception e) {
            log.error("Error resetting WireMock", e);
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "Error resetting WireMock", e);
        }
    }

    private UpdatePercentageResponse updateMockRequest(Double newPercentage, HttpEntity<String> request) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    WIREMOCK_ADMIN_URL + "/mappings",
                    HttpMethod.POST,
                    request,
                    String.class
            );

            log.info("WireMock updated with percentage: {}", newPercentage);
            return UpdatePercentageResponse.builder()
                    .newPercentage(newPercentage)
                    .build();
        } catch (Exception e) {
            log.error("Error updating WireMock", e);
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Error updating WireMock", e);
        }
    }

    private static HttpEntity<String> createMockRequest(Double newPercentage) {
        String jsonBody = """
            {
              "request": {
                "method": "GET",
                "url": "/external-percentage"
              },
              "response": {
                "status": 200,
                "jsonBody": { "percentage": %s },
                "headers": { "Content-Type": "application/json" }
              }
            }
        """.formatted(newPercentage);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return new HttpEntity<>(jsonBody, headers);
    }
}
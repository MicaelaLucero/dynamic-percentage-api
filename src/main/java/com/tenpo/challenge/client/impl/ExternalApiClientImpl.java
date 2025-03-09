package com.tenpo.challenge.client.impl;

import com.tenpo.challenge.client.ExternalApiClient;
import com.tenpo.challenge.dto.UpdatePercentageResponse;
import com.tenpo.challenge.exception.ExternalApiException;
import com.tenpo.challenge.exception.InvalidApiResponseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

import static com.tenpo.challenge.utils.Constants.EXTERNAL_API_URL;
import static com.tenpo.challenge.utils.Constants.WIREMOCK_ADMIN_URL;

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
            throw new InvalidApiResponseException("Invalid response from external API");
        } catch (InvalidApiResponseException e) {
            log.error("Invalid response format from external API", e);
            throw e;
        } catch (Exception e) {
            log.error("Error connecting to external API", e);
            throw new ExternalApiException("External API unavailable", e);
        }
    }

    @Override
    public UpdatePercentageResponse updatePercentage(Double newPercentage) {
        try {
            resetWireMock();
            HttpEntity<String> request = createMockRequest(newPercentage);
            return updateMockRequest(newPercentage, request);
        } catch (Exception e) {
            log.error("Error updating external API with new percentage: {}", newPercentage, e);
            throw new ExternalApiException("Failed to update percentage in external API", e);
        }
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
            log.error("Failed to reset WireMock mappings", e);
            throw new ExternalApiException("WireMock reset failed", e);
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
            log.error("Failed to update WireMock with percentage: {}", newPercentage, e);
            throw new ExternalApiException("Failed to update WireMock", e);
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
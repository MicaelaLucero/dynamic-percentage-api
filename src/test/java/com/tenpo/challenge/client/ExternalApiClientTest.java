package com.tenpo.challenge.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.tenpo.challenge.client.impl.ExternalApiClientImpl;
import com.tenpo.challenge.config.properties.ExternalApiProperties;
import com.tenpo.challenge.model.dto.UpdatePercentageResponse;
import com.tenpo.challenge.exception.ExternalApiException;
import com.tenpo.challenge.exception.InvalidResponseException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Slf4j
class ExternalApiClientTest {

    private static final int WIREMOCK_PORT = 8085;
    private static WireMockServer wireMockServer;
    @MockitoBean
    private ExternalApiClientImpl externalApiClient;

    @BeforeAll
    static void startWireMock() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(WireMockConfiguration.options().port(WIREMOCK_PORT));
            wireMockServer.start();
            await().atMost(5, TimeUnit.SECONDS).until(() -> wireMockServer.isRunning());
            WireMock.configureFor("localhost", WIREMOCK_PORT);
        }
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setup() {
        RestTemplate restTemplate = new RestTemplate();
        String baseUrl = "http://localhost:" + WIREMOCK_PORT;
        externalApiClient = new ExternalApiClientImpl(restTemplate,
                new ExternalApiProperties(
                        baseUrl + "/external-percentage",
                        baseUrl + "/__admin"));

        wireMockServer.resetAll();

        stubFor(get(urlEqualTo("/external-percentage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"percentage\": 15.0}")));
    }

    @Test
    void shouldReturnPercentageFromExternalApi() {
        Double percentage = externalApiClient.getPercentage();
        assertNotNull(percentage);
        assertEquals(15.0, percentage);
    }

    @Test
    void shouldThrowInvalidResponseExceptionForInvalidApiResponse() {
        stubFor(get(urlEqualTo("/external-percentage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"wrongKey\": 15.0}")));

        assertThrows(InvalidResponseException.class, () -> externalApiClient.getPercentage());
    }

    @Test
    void shouldThrowExternalApiExceptionWhenApiFails() {
        stubFor(get(urlEqualTo("/external-percentage"))
                .willReturn(aResponse().withStatus(500)));

        assertThrows(ExternalApiException.class, () -> externalApiClient.getPercentage());
    }

    @Test
    void shouldUpdatePercentageSuccessfully() {
        UpdatePercentageResponse percentage = externalApiClient.updatePercentage(20.0);
        assertNotNull(percentage);
        assertEquals(20.0, percentage.getNewPercentage());
    }

    @Test
    void shouldThrowExceptionWhenResetFails() {
        RestTemplate restTemplateMock = mock(RestTemplate.class);
        ExternalApiClientImpl apiClient = new ExternalApiClientImpl(
                restTemplateMock, new ExternalApiProperties(
                "http://localhost:8085/external-percentage",
                "http://localhost:8085/__admin"));

        when(restTemplateMock.exchange(
                eq("http://localhost:8085/__admin/mappings/reset"),
                eq(HttpMethod.POST),
                eq(HttpEntity.EMPTY),
                eq(String.class))
        ).thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));

        ExternalApiException exception = assertThrows(ExternalApiException.class, () -> apiClient.updatePercentage(10.0));

        assertTrue(exception.getMessage().contains("WireMock reset failed"));
    }
}
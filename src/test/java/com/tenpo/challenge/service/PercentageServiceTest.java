package com.tenpo.challenge.service;

import com.tenpo.challenge.client.ExternalApiClient;
import com.tenpo.challenge.model.dto.PercentageResponse;
import com.tenpo.challenge.model.dto.UpdatePercentageResponse;
import com.tenpo.challenge.exception.ExternalApiException;
import com.tenpo.challenge.exception.InvalidPercentageException;
import com.tenpo.challenge.exception.PercentageUnavailableException;
import com.tenpo.challenge.repository.PercentageCacheRepository;
import com.tenpo.challenge.service.impl.PercentageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PercentageServiceTest {

    @Mock
    private ExternalApiClient externalApiClient;

    @Mock
    private PercentageCacheRepository cacheRepository;

    @InjectMocks
    private PercentageServiceImpl percentageService;

    private static final double VALID_PERCENTAGE = 10.0;

    @BeforeEach
    void setUp() {
        reset(externalApiClient, cacheRepository);
    }

    @Test
    void shouldReturnPercentageFromExternalApiAndCacheIt() {
        when(externalApiClient.getPercentage()).thenReturn(VALID_PERCENTAGE);

        PercentageResponse response = percentageService.getPercentage();

        assertNotNull(response);
        assertEquals(VALID_PERCENTAGE, response.getPercentage());
        assertEquals("EXTERNAL_API", response.getSource());

        verify(externalApiClient, times(1)).getPercentage();
        verify(cacheRepository, times(1)).savePercentage(VALID_PERCENTAGE);
    }

    @Test
    void shouldReturnPercentage() {
        when(externalApiClient.getPercentage()).thenReturn(null);

        PercentageResponse response = percentageService.getPercentage();

        assertNotNull(response);
        assertNull(response.getPercentage());
        assertEquals("EXTERNAL_API", response.getSource());

        verify(externalApiClient, times(1)).getPercentage();
        verify(cacheRepository, times(1)).savePercentage(null);
    }

    @Test
    void shouldReturnCachedPercentageWhenExternalApiFails() {
        when(externalApiClient.getPercentage()).thenThrow(new ExternalApiException("External API unavailable"));
        when(cacheRepository.getCachedPercentage()).thenReturn(VALID_PERCENTAGE);

        PercentageResponse response = percentageService.getPercentage();

        assertNotNull(response);
        assertEquals(VALID_PERCENTAGE, response.getPercentage());
        assertEquals("CACHE", response.getSource());

        verify(externalApiClient, times(1)).getPercentage();
        verify(cacheRepository, times(1)).getCachedPercentage();
    }

    @Test
    void shouldThrowPercentageUnavailableExceptionWhenApiAndCacheAreUnavailable() {
        when(externalApiClient.getPercentage()).thenThrow(new ExternalApiException("External API unavailable"));
        when(cacheRepository.getCachedPercentage()).thenReturn(null);

        PercentageUnavailableException exception = assertThrows(
                PercentageUnavailableException.class,
                () -> percentageService.getPercentage()
        );

        assertEquals("External API failed, and no cached value is available.", exception.getMessage());

        verify(externalApiClient, times(1)).getPercentage();
        verify(cacheRepository, times(1)).getCachedPercentage();
    }

    @Test
    void shouldUpdatePercentageSuccessfully() {
        UpdatePercentageResponse expectedResponse = new UpdatePercentageResponse(VALID_PERCENTAGE);
        when(externalApiClient.updatePercentage(VALID_PERCENTAGE)).thenReturn(expectedResponse);

        UpdatePercentageResponse response = percentageService.updatePercentage(VALID_PERCENTAGE);

        assertNotNull(response);
        assertEquals(VALID_PERCENTAGE, response.getNewPercentage());

        verify(externalApiClient, times(1)).updatePercentage(VALID_PERCENTAGE);
    }

    @Test
    void shouldThrowInvalidPercentageExceptionForNegativeValue() {
        InvalidPercentageException exception = assertThrows(
                InvalidPercentageException.class,
                () -> percentageService.updatePercentage(-5.0)
        );

        assertEquals("Percentage must be non-negative", exception.getMessage());
        verify(externalApiClient, never()).updatePercentage(anyDouble());
    }

    @Test
    void shouldThrowInvalidPercentageExceptionForNullValue() {
        InvalidPercentageException exception = assertThrows(
                InvalidPercentageException.class,
                () -> percentageService.updatePercentage(null)
        );

        assertEquals("Percentage must be non-negative", exception.getMessage());
        verify(externalApiClient, never()).updatePercentage(anyDouble());
    }

    @Test
    void shouldThrowExternalApiExceptionWhenUpdateFails() {
        when(externalApiClient.updatePercentage(VALID_PERCENTAGE))
                .thenThrow(new ExternalApiException("Failed to update percentage"));

        ExternalApiException exception = assertThrows(
                ExternalApiException.class,
                () -> percentageService.updatePercentage(VALID_PERCENTAGE)
        );

        assertEquals("Failed to update percentage", exception.getMessage());
        verify(externalApiClient, times(1)).updatePercentage(VALID_PERCENTAGE);
    }
}
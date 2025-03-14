package com.tenpo.challenge.service.impl;

import com.tenpo.challenge.client.ExternalApiClient;
import com.tenpo.challenge.model.dto.PercentageResponse;
import com.tenpo.challenge.model.dto.UpdatePercentageResponse;
import com.tenpo.challenge.exception.ExternalApiException;
import com.tenpo.challenge.exception.InvalidPercentageException;
import com.tenpo.challenge.exception.PercentageUnavailableException;
import com.tenpo.challenge.repository.PercentageCacheRepository;
import com.tenpo.challenge.service.PercentageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.tenpo.challenge.utils.Constants.CACHE;
import static com.tenpo.challenge.utils.Constants.EXTERNAL_API;

@Service
@Slf4j
@RequiredArgsConstructor
public class PercentageServiceImpl implements PercentageService {

    private final ExternalApiClient externalApiClient;
    private final PercentageCacheRepository cacheRepository;

    @Override
    public PercentageResponse getPercentage() {
        log.info("Searching percentage in external API...");

        try {
            Double percentage = externalApiClient.getPercentage();
            cacheRepository.savePercentage(percentage);
            return new PercentageResponse(percentage, EXTERNAL_API);

        } catch (ExternalApiException e) {
            log.warn("External API failed, searching percentage in Redis cache...");

            Double cachedValue = cacheRepository.getCachedPercentage();
            if (cachedValue != null) {
                log.info("Using cached value: {}", cachedValue);
                return new PercentageResponse(cachedValue, CACHE);
            }

            log.error("External API failed, and no cached value is available.");
            throw new PercentageUnavailableException("External API failed, and no cached value is available.");
        }
    }

    @Override
    public UpdatePercentageResponse updatePercentage(Double newPercentage) {
        if (newPercentage == null || newPercentage < 0) {
            throw new InvalidPercentageException("Percentage must be non-negative");
        }
        try {
            return externalApiClient.updatePercentage(newPercentage);
        } catch (ExternalApiException e) {
            log.error("External API update failed: {}", e.getMessage());
            throw e;
        }
    }
}
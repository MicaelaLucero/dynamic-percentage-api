package com.tenpo.challenge.service.impl;

import com.tenpo.challenge.client.ExternalApiClient;
import com.tenpo.challenge.repository.PercentageCacheRepository;
import com.tenpo.challenge.service.PercentageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Service
@Slf4j
@RequiredArgsConstructor
public class PercentageServiceImpl implements PercentageService {

    private final ExternalApiClient externalApiClient;
    private final PercentageCacheRepository cacheRepository;

    @Override
    public Double getPercentage() {
        log.info("Searching in Redis cache...");
        Double cachedValue = cacheRepository.getCachedPercentage();
        if (cachedValue != null) {
            return cachedValue;
        }

        log.warn("No value found in cache. Searching in external API...");
        try {
            Double percentage = externalApiClient.getPercentage();
            cacheRepository.savePercentage(percentage);
            return percentage;
        } catch (Exception e) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "External API unavailable", e);
        }
    }

    @Override
    public String updatePercentage(Double newPercentage) {
        try {
            return externalApiClient.updatePercentage(newPercentage);
        } catch (Exception e) {
            throw new ResponseStatusException(SERVICE_UNAVAILABLE, "External API unavailable", e);
        }
    }
}
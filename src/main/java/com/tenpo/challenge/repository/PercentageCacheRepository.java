package com.tenpo.challenge.repository;

public interface PercentageCacheRepository {
    Double getCachedPercentage();
    void savePercentage(Double percentage);
}
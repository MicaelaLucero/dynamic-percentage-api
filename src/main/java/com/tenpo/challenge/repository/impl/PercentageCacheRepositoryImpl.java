package com.tenpo.challenge.repository.impl;

import com.tenpo.challenge.repository.PercentageCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

import static com.tenpo.challenge.utils.Constants.CACHE_EXPIRATION_SECONDS;
import static com.tenpo.challenge.utils.Constants.CACHE_KEY;

@Repository
@Slf4j
@RequiredArgsConstructor
public class PercentageCacheRepositoryImpl implements PercentageCacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public Double getCachedPercentage() {
        log.info("Checking cache in Redis...");
        Object cachedValue = redisTemplate.opsForValue().get(CACHE_KEY);

        if (cachedValue instanceof Number) {
            log.info("Cache hit: {}", cachedValue);
            return ((Number) cachedValue).doubleValue();
        }
        return null;
    }

    @Override
    public void savePercentage(Double percentage) {
        if (percentage != null) {
            redisTemplate.opsForValue().set(CACHE_KEY, percentage, CACHE_EXPIRATION_SECONDS, TimeUnit.SECONDS);
            log.info("Saved to Redis: {} (expires in {} seconds)", percentage, CACHE_EXPIRATION_SECONDS);
        }
    }
}
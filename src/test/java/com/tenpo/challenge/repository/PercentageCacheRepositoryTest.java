package com.tenpo.challenge.repository;

import com.tenpo.challenge.exception.CacheException;
import com.tenpo.challenge.repository.impl.PercentageCacheRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static com.tenpo.challenge.utils.Constants.CACHE_EXPIRATION_MINUTES;
import static com.tenpo.challenge.utils.Constants.CACHE_KEY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PercentageCacheRepositoryTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private PercentageCacheRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getCachedPercentage_shouldReturnValue_whenCacheHit() {
        when(valueOperations.get(CACHE_KEY)).thenReturn(12.5);

        Double result = repository.getCachedPercentage();

        assertNotNull(result);
        assertEquals(12.5, result);
    }

    @Test
    void getCachedPercentage_shouldReturnNull_whenCacheMiss() {
        when(valueOperations.get(CACHE_KEY)).thenReturn(null);

        Double result = repository.getCachedPercentage();

        assertNull(result);
    }

    @Test
    void getCachedPercentage_shouldThrowCacheException_onRedisError() {
        when(valueOperations.get(CACHE_KEY)).thenThrow(new RuntimeException("Redis error"));

        assertThrows(CacheException.class, repository::getCachedPercentage);
    }

    @Test
    void savePercentage_shouldSaveSuccessfully() {
        doNothing().when(valueOperations).set(CACHE_KEY, 15.0, CACHE_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        assertDoesNotThrow(() -> repository.savePercentage(15.0));
    }

    @Test
    void savePercentage_shouldThrowCacheException_onRedisError() {
        doThrow(new RuntimeException("Redis error"))
                .when(valueOperations).set(CACHE_KEY, 15.0, CACHE_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        assertThrows(CacheException.class, () -> repository.savePercentage(15.0));
    }
}
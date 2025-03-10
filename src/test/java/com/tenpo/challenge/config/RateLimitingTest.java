package com.tenpo.challenge.config;

import com.tenpo.challenge.config.security.RateLimitingInterceptor;
import com.tenpo.challenge.exception.RateLimitException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RateLimitingTest {

    private RateLimitingInterceptor rateLimitingInterceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        rateLimitingInterceptor = new RateLimitingInterceptor();

        ReflectionTestUtils.setField(rateLimitingInterceptor, "requestLimit", 2);
        ReflectionTestUtils.setField(rateLimitingInterceptor, "timeWindowSeconds", 10);

        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
    }

    @Test
    void shouldAllowRequestsWithinLimit() {
        IntStream.range(0, 2).forEach(i ->
                assertDoesNotThrow(() -> rateLimitingInterceptor.preHandle(request, response, new Object()))
        );
    }

    @Test
    void shouldBlockRequestsExceedingLimit() {
        IntStream.range(0, 2).forEach(i ->
                assertDoesNotThrow(() -> rateLimitingInterceptor.preHandle(request, response, new Object()))
        );

        RateLimitException exception = assertThrows(RateLimitException.class,
                () -> rateLimitingInterceptor.preHandle(request, response, new Object()));

        assertEquals("Too many requests. Please try again later.", exception.getMessage());

        assertEquals(10, exception.getRetryAfterSeconds(), "El tiempo de espera debería ser 10 segundos");
    }
}


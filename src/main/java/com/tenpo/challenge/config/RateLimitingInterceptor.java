package com.tenpo.challenge.config;

import io.github.bucket4j.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${ratelimit.requests}")
    private int requestLimit;

    @Value("${ratelimit.time-window-seconds}")
    private long timeWindowSeconds;

    @Override
    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response,@NonNull Object handler) throws IOException {
        String clientIp = request.getRemoteAddr();

        Bucket bucket = buckets.computeIfAbsent(clientIp, key -> createNewBucket());

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Too many requests. Please try again later.\"}");
            return false;
        }
    }

    private Bucket createNewBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(requestLimit).refillGreedy(requestLimit, Duration.ofSeconds(timeWindowSeconds)))
                .build();
    }
}
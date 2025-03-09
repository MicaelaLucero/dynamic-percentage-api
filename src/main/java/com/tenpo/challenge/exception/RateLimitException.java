package com.tenpo.challenge.exception;

import lombok.Getter;

@Getter
public class RateLimitException extends RuntimeException {
    private final long retryAfterSeconds;

    public RateLimitException(String message, long retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
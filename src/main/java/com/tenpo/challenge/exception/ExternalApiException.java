package com.tenpo.challenge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
public class ExternalApiException extends RuntimeException {
    public ExternalApiException(String message) {
        super(message);
    }

    public ExternalApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
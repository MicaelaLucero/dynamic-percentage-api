package com.tenpo.challenge.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleExternalApiException() {
        ExternalApiException exception = new ExternalApiException("API is down");
        ResponseEntity<Object> response = handler.handleExternalApiException(exception);

        assertEquals(503, response.getStatusCode().value());
        assertEquals("External API error", ((Map<?, ?>) response.getBody()).get("error"));
        assertEquals("API is down", ((Map<?, ?>) response.getBody()).get("message"));
    }

    @Test
    void shouldHandleRateLimitException() {
        RateLimitException exception = new RateLimitException("Too many requests", 30);
        ResponseEntity<Object> response = handler.handleRateLimitException(exception);

        assertEquals(429, response.getStatusCode().value());
        assertEquals("Too Many Requests", ((Map<?, ?>) response.getBody()).get("error"));
        assertEquals("Too many requests", ((Map<?, ?>) response.getBody()).get("message"));
        assertEquals("30 seconds", ((Map<?, ?>) response.getBody()).get("retryAfter"));
        assertTrue(response.getHeaders().containsKey("Retry-After"));
    }

    @Test
    void shouldHandleValidationException() {
        BindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "objectName");
        bindingResult.addError(new FieldError("objectName", "fieldName", "must not be null"));

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<Object> response = handler.handleValidationException(exception);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Validation Error", ((Map<?, ?>) response.getBody()).get("error"));
        assertTrue(((List<?>) ((Map<?, ?>) response.getBody()).get("message")).contains("fieldName: must not be null"));
    }

    @Test
    void shouldHandleGenericException() {
        Exception exception = new Exception("Unexpected error occurred");
        ResponseEntity<Object> response = handler.handleGenericException(exception);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Unexpected error", ((Map<?, ?>) response.getBody()).get("error"));
        assertEquals("Unexpected error occurred", ((Map<?, ?>) response.getBody()).get("message"));
    }
}

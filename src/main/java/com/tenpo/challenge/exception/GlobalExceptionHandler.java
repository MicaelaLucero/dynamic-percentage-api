package com.tenpo.challenge.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<Object> handleExternalApiException(ExternalApiException ex) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "External API error", ex.getMessage());
    }

    @ExceptionHandler(InvalidResponseException.class)
    public ResponseEntity<Object> handleInvalidApiResponseException(InvalidResponseException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Invalid response", ex.getMessage());
    }

    @ExceptionHandler(InvalidPercentageException.class)
    public ResponseEntity<Object> handleInvalidPercentageException(InvalidPercentageException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, "Invalid percentage value", ex.getMessage());
    }

    @ExceptionHandler(CacheException.class)
    public ResponseEntity<Object> handleCacheException(CacheException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Cache error", ex.getMessage());
    }

    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<Object> handleDatabaseException(DatabaseException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Database error", ex.getMessage());
    }

    @ExceptionHandler(PercentageUnavailableException.class)
    public ResponseEntity<Object> handlePercentageUnavailableException(PercentageUnavailableException ex) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Percentage unavailable", ex.getMessage());
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<Object> handleRateLimitException(RateLimitException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
        body.put("error", "Too Many Requests");
        body.put("message", ex.getMessage());
        body.put("retryAfter", ex.getRetryAfterSeconds() + " seconds");

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", String.valueOf(ex.getRetryAfterSeconds()))
                .body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Error");
        body.put("message", errors);

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", ex.getMessage());
    }

    private ResponseEntity<Object> buildResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);

        return new ResponseEntity<>(body, status);
    }
}
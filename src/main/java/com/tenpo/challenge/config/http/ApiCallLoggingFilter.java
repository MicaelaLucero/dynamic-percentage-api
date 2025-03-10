package com.tenpo.challenge.config.http;

import com.tenpo.challenge.model.entity.ApiCallHistoryEntity;
import com.tenpo.challenge.service.ApiCallHistoryService;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiCallLoggingFilter implements Filter {

    private final ApiCallHistoryService apiCallHistoryService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, jakarta.servlet.ServletException {
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);

        if (wrappedRequest.getRequestURI().startsWith("/api/v1/history")) {
            chain.doFilter(request, response);
            return;
        }

        chain.doFilter(wrappedRequest, wrappedResponse);

        String method = wrappedRequest.getMethod();
        String endpoint = wrappedRequest.getRequestURI();
        String queryParams = getQueryParams(wrappedRequest);

        ApiCallHistoryEntity apiCallHistoryEntity = ApiCallHistoryEntity.builder()
                .method(method)
                .endpoint(endpoint + (queryParams.isEmpty() ? "" : "?" + queryParams))
                .statusCode(wrappedResponse.getStatus())
                .requestBody(getRequestBody(wrappedRequest))
                .responseBody(getResponseBody(wrappedResponse))
                .timestamp(LocalDateTime.now())
                .build();

        apiCallHistoryService.saveApiCall(apiCallHistoryEntity);

        log.info("API Call Logged: [{} {}] -> Status: {}", method, endpoint, wrappedResponse.getStatus());

        wrappedResponse.copyBodyToResponse();
    }

    private String getQueryParams(ContentCachingRequestWrapper request) {
        return request.getParameterMap().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
                .collect(Collectors.joining("&"));
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        return (content.length > 0) ? new String(content, StandardCharsets.UTF_8) : "";
    }

    private String getResponseBody(ContentCachingResponseWrapper response) throws IOException {
        byte[] content = response.getContentAsByteArray();
        String body = (content.length > 0) ? new String(content, StandardCharsets.UTF_8) : "";
        response.copyBodyToResponse();
        return body;
    }
}
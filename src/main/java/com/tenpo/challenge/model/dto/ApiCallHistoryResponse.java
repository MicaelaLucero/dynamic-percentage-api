package com.tenpo.challenge.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ApiCallHistoryResponse {
    private String method;
    private String endpoint;
    private int statusCode;
    private String requestBody;
    private String responseBody;
    private LocalDateTime timestamp;
}

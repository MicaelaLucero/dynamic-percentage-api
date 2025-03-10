package com.tenpo.challenge.model.mapper;

import com.tenpo.challenge.model.dto.ApiCallHistoryResponse;
import com.tenpo.challenge.model.dto.PaginatedResponse;
import com.tenpo.challenge.model.entity.ApiCallHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ApiCallHistoryMapper {

    public ApiCallHistoryResponse toDto(ApiCallHistoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return ApiCallHistoryResponse.builder()
                .method(entity.getMethod())
                .endpoint(entity.getEndpoint())
                .statusCode(entity.getStatusCode())
                .requestBody(entity.getRequestBody())
                .responseBody(entity.getResponseBody())
                .timestamp(entity.getTimestamp())
                .build();
    }

    public PaginatedResponse<ApiCallHistoryResponse> toPaginatedResponse(Page<ApiCallHistoryEntity> page) {
        List<ApiCallHistoryResponse> data = page.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return PaginatedResponse.<ApiCallHistoryResponse>builder()
                .data(data)
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }
}

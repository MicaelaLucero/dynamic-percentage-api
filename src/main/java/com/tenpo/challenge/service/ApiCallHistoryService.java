package com.tenpo.challenge.service;

import com.tenpo.challenge.model.dto.ApiCallHistoryResponse;
import com.tenpo.challenge.model.dto.PaginatedResponse;
import com.tenpo.challenge.model.entity.ApiCallHistoryEntity;
import org.springframework.data.domain.Pageable;

public interface ApiCallHistoryService {
    PaginatedResponse<ApiCallHistoryResponse> getHistory(Pageable pageable);
    void saveApiCall(ApiCallHistoryEntity apiCallHistoryEntity);
}

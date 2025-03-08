package com.tenpo.challenge.service;

import com.tenpo.challenge.dto.PaginatedResponse;
import com.tenpo.challenge.entity.ApiCallHistory;
import org.springframework.data.domain.Pageable;

public interface ApiCallHistoryService {

    PaginatedResponse<ApiCallHistory> getHistory(Pageable pageable);
    void saveApiCall(ApiCallHistory apiCallHistory);
}

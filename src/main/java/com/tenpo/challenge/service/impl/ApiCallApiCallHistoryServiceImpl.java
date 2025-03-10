package com.tenpo.challenge.service.impl;

import com.tenpo.challenge.model.dto.ApiCallHistoryResponse;
import com.tenpo.challenge.model.dto.PaginatedResponse;
import com.tenpo.challenge.model.entity.ApiCallHistoryEntity;
import com.tenpo.challenge.exception.DataIntegrityException;
import com.tenpo.challenge.exception.DatabaseException;
import com.tenpo.challenge.model.mapper.ApiCallHistoryMapper;
import com.tenpo.challenge.repository.ApiCallHistoryRepository;
import com.tenpo.challenge.service.ApiCallHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiCallApiCallHistoryServiceImpl implements ApiCallHistoryService {

    private final ApiCallHistoryRepository historyRepository;
    private final ApiCallHistoryMapper apiCallHistoryMapper;

    @Override
    public PaginatedResponse<ApiCallHistoryResponse> getHistory(Pageable pageable) {
        try {
            Page<ApiCallHistoryEntity> page = historyRepository.findAll(pageable);
            return apiCallHistoryMapper.toPaginatedResponse(page);
        } catch (Exception e) {
            log.error("Error retrieving API call history from database", e);
            throw new DatabaseException("Error retrieving history from database", e);
        }
    }

    @Async
    @Override
    public void saveApiCall(ApiCallHistoryEntity apiCallHistoryEntity) {
        try {
            historyRepository.save(apiCallHistoryEntity);
            log.info("API Call history saved successfully: [{} {}] -> Status: {}",
                    apiCallHistoryEntity.getMethod(), apiCallHistoryEntity.getEndpoint(), apiCallHistoryEntity.getStatusCode());
        } catch (DataIntegrityException e) {
            log.error("Data integrity violation while saving API call history, but request will continue", e);
        } catch (Exception e) {
            log.error("Failed to save API call history, but request will continue", e);
        }
    }
}

package com.tenpo.challenge.service.impl;

import com.tenpo.challenge.dto.PaginatedResponse;
import com.tenpo.challenge.entity.ApiCallHistory;
import com.tenpo.challenge.exception.DatabaseException;
import com.tenpo.challenge.repository.ApiCallHistoryRepository;
import com.tenpo.challenge.service.ApiCallHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiCallApiCallHistoryServiceImpl implements ApiCallHistoryService {

    private final ApiCallHistoryRepository historyRepository;

    @Override
    public PaginatedResponse<ApiCallHistory> getHistory(Pageable pageable) {
        try {
            Page<ApiCallHistory> page = historyRepository.findAll(pageable);

            return PaginatedResponse.<ApiCallHistory>builder()
                    .data(page.getContent())
                    .currentPage(page.getNumber())
                    .totalPages(page.getTotalPages())
                    .totalElements(page.getTotalElements())
                    .build();
        } catch (Exception e) {
            log.error("Error retrieving API call history from database", e);
            throw new DatabaseException("Error retrieving history from database", e);
        }
    }

    @Async
    @Override
    public void saveApiCall(ApiCallHistory apiCallHistory) {
        try {
            historyRepository.save(apiCallHistory);
            log.info("API Call history saved successfully: [{} {}] -> Status: {}",
                    apiCallHistory.getMethod(), apiCallHistory.getEndpoint(), apiCallHistory.getStatusCode());
        } catch (DataIntegrityViolationException e) {
            log.error("Data integrity violation while saving API call history, but request will continue", e);
        } catch (Exception e) {
            log.error("Failed to save API call history, but request will continue", e);
        }
    }
}

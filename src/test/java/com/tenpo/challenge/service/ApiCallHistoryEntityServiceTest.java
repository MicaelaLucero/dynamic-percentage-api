package com.tenpo.challenge.service;

import com.tenpo.challenge.model.dto.ApiCallHistoryResponse;
import com.tenpo.challenge.model.dto.PaginatedResponse;
import com.tenpo.challenge.model.entity.ApiCallHistoryEntity;
import com.tenpo.challenge.exception.DataIntegrityException;
import com.tenpo.challenge.exception.DatabaseException;
import com.tenpo.challenge.model.mapper.ApiCallHistoryMapper;
import com.tenpo.challenge.repository.ApiCallHistoryRepository;
import com.tenpo.challenge.service.impl.ApiCallApiCallHistoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiCallHistoryEntityServiceTest {

    @Mock
    private ApiCallHistoryRepository historyRepository;
    @Mock
    private ApiCallHistoryMapper apiCallHistoryMapper;
    @InjectMocks
    private ApiCallApiCallHistoryServiceImpl service;

    private ApiCallHistoryEntity apiCallHistoryEntity1;
    private ApiCallHistoryEntity apiCallHistoryEntity2;
    private ApiCallHistoryResponse apiCallHistoryResponse1;
    private ApiCallHistoryResponse apiCallHistoryResponse2;

    @BeforeEach
    void setUp() {
        apiCallHistoryEntity1 = new ApiCallHistoryEntity(1L, "GET", "/test1", 200, "{}", "{}", LocalDateTime.now());
        apiCallHistoryEntity2 = new ApiCallHistoryEntity(2L, "POST", "/test2", 201, "{}", "{}", LocalDateTime.now());

        apiCallHistoryResponse1 = ApiCallHistoryResponse.builder()
                .method(apiCallHistoryEntity1.getMethod())
                .endpoint(apiCallHistoryEntity1.getEndpoint())
                .statusCode(apiCallHistoryEntity1.getStatusCode())
                .requestBody(apiCallHistoryEntity1.getRequestBody())
                .responseBody(apiCallHistoryEntity1.getResponseBody())
                .timestamp(apiCallHistoryEntity1.getTimestamp())
                .build();

        apiCallHistoryResponse2 = ApiCallHistoryResponse.builder()
                .method(apiCallHistoryEntity2.getMethod())
                .endpoint(apiCallHistoryEntity2.getEndpoint())
                .statusCode(apiCallHistoryEntity2.getStatusCode())
                .requestBody(apiCallHistoryEntity2.getRequestBody())
                .responseBody(apiCallHistoryEntity2.getResponseBody())
                .timestamp(apiCallHistoryEntity2.getTimestamp())
                .build();
    }

    @Test
    void getHistory_shouldReturnPaginatedResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ApiCallHistoryEntity> page = new PageImpl<>(Collections.singletonList(apiCallHistoryEntity1), pageable, 1);

        when(historyRepository.findAll(pageable)).thenReturn(page);
        when(apiCallHistoryMapper.toPaginatedResponse(page))
                .thenReturn(new PaginatedResponse<>(0, 1, 1, Collections.singletonList(apiCallHistoryResponse1)));

        PaginatedResponse<ApiCallHistoryResponse> response = service.getHistory(pageable);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(0, response.getCurrentPage());
        assertFalse(response.getData().isEmpty());
    }

    @Test
    void getHistory_shouldReturnNonEmptyResponse() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ApiCallHistoryEntity> page = new PageImpl<>(Arrays.asList(apiCallHistoryEntity1, apiCallHistoryEntity2), pageable, 2);

        when(historyRepository.findAll(pageable)).thenReturn(page);

        when(apiCallHistoryMapper.toPaginatedResponse(page))
                .thenReturn(new PaginatedResponse<>(0, 1, 2,Arrays.asList(apiCallHistoryResponse1, apiCallHistoryResponse2)));

        PaginatedResponse<ApiCallHistoryResponse> response = service.getHistory(pageable);

        assertNotNull(response);
        assertEquals(2, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertEquals(0, response.getCurrentPage());
        assertEquals(2, response.getData().size());
        assertFalse(response.getData().isEmpty());
    }


    @Test
    void getHistory_shouldThrowDatabaseException_onRepositoryError() {
        Pageable pageable = PageRequest.of(0, 10);

        when(historyRepository.findAll(pageable)).thenThrow(new RuntimeException("DB error"));

        assertThrows(DatabaseException.class, () -> service.getHistory(pageable));
    }

    @Test
    void saveApiCall_shouldSaveSuccessfully() {
        when(historyRepository.save(apiCallHistoryEntity1)).thenReturn(apiCallHistoryEntity1);

        assertDoesNotThrow(() -> service.saveApiCall(apiCallHistoryEntity1));
    }


    @Test
    void saveApiCall_shouldHandleDataIntegrityException() {
        doThrow(new DataIntegrityException("Data Integrity Violation"))
                .when(historyRepository).save(apiCallHistoryEntity1);

        assertDoesNotThrow(() -> service.saveApiCall(apiCallHistoryEntity1));
    }

    @Test
    void saveApiCall_shouldHandleGeneralException() {
        doThrow(new RuntimeException("Unexpected error"))
                .when(historyRepository).save(apiCallHistoryEntity1);

        assertDoesNotThrow(() -> service.saveApiCall(apiCallHistoryEntity1));

        verify(historyRepository, times(1)).save(apiCallHistoryEntity1);
    }
}
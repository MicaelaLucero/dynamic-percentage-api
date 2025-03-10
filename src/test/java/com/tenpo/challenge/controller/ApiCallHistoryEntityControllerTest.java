package com.tenpo.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenpo.challenge.model.dto.ApiCallHistoryResponse;
import com.tenpo.challenge.model.dto.PaginatedResponse;
import com.tenpo.challenge.model.entity.ApiCallHistoryEntity;
import com.tenpo.challenge.service.ApiCallHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApiCallHistoryController.class)
class ApiCallHistoryEntityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApiCallHistoryService apiCallHistoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ApiCallHistoryResponse historyEntry = ApiCallHistoryResponse.builder()
                .method("GET")
                .endpoint("/api/v1/test")
                .statusCode(200)
                .requestBody("")
                .responseBody("{\"message\": \"Success\"}")
                .timestamp(LocalDateTime.now())
                .build();

        List<ApiCallHistoryResponse> historyList = List.of(historyEntry);
        PaginatedResponse<ApiCallHistoryResponse> mockResponse = new PaginatedResponse<>(0, 1, 1,historyList);

        Mockito.when(apiCallHistoryService.getHistory(any(Pageable.class))).thenReturn(mockResponse);
    }

    @Test
    void shouldReturnApiCallHistory() throws Exception {
        mockMvc.perform(get("/api/v1/history")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].method").value("GET"))
                .andExpect(jsonPath("$.data[0].endpoint").value("/api/v1/test"))
                .andExpect(jsonPath("$.data[0].statusCode").value(200));
    }
}
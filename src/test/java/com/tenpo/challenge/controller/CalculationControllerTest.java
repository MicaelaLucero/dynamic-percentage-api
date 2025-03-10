package com.tenpo.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenpo.challenge.config.http.ApiCallLoggingFilter;
import com.tenpo.challenge.model.dto.CalculationRequest;
import com.tenpo.challenge.model.dto.CalculationResponse;
import com.tenpo.challenge.service.ApiCallHistoryService;
import com.tenpo.challenge.service.CalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CalculationController.class)
class CalculationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalculationService calculationService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApiCallHistoryService apiCallHistoryService;

    @BeforeEach
    void setUp(WebApplicationContext context) {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilters(new ApiCallLoggingFilter(apiCallHistoryService))
                .build();
    }

    @BeforeEach
    void setUp() {
        CalculationResponse mockResponse = CalculationResponse.builder()
                .firstNumber(100.0)
                .secondNumber(50.0)
                .percentage(10.0)
                .result(165.0)
                .build();

        Mockito.when(calculationService.calculate(any(CalculationRequest.class))).thenReturn(mockResponse);
    }

    @Test
    void shouldReturnCalculationResult() throws Exception {
        CalculationRequest request = new CalculationRequest(100.0, 50.0);

        mockMvc.perform(post("/api/v1/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstNumber").value(100.0))
                .andExpect(jsonPath("$.secondNumber").value(50.0))
                .andExpect(jsonPath("$.percentage").value(10.0))
                .andExpect(jsonPath("$.result").value(165.0));
    }
}
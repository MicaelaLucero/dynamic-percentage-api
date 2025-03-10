package com.tenpo.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tenpo.challenge.config.http.ApiCallLoggingFilter;
import com.tenpo.challenge.model.dto.PercentageResponse;
import com.tenpo.challenge.model.dto.UpdatePercentageResponse;
import com.tenpo.challenge.service.ApiCallHistoryService;
import com.tenpo.challenge.service.PercentageService;
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

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PercentageController.class)
class PercentageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PercentageService percentageService;

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
        PercentageResponse mockPercentageResponse = new PercentageResponse(10.0, "EXTERNAL_API");
        UpdatePercentageResponse mockUpdateResponse = new UpdatePercentageResponse(15.0);

        Mockito.when(percentageService.getPercentage()).thenReturn(mockPercentageResponse);
        Mockito.when(percentageService.updatePercentage(anyDouble())).thenReturn(mockUpdateResponse);
    }

    @Test
    void shouldReturnPercentage() throws Exception {
        mockMvc.perform(get("/api/v1/percentage")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.percentage").value(10.0))
                .andExpect(jsonPath("$.source").value("EXTERNAL_API"));
    }

    @Test
    void shouldUpdatePercentage() throws Exception {
        mockMvc.perform(put("/api/v1/percentage?newPercentage=15.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.newPercentage").value(15.0));
    }
}
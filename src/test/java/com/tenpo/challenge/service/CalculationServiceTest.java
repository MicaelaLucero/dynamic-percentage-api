package com.tenpo.challenge.service;

import com.tenpo.challenge.model.dto.CalculationRequest;
import com.tenpo.challenge.model.dto.CalculationResponse;
import com.tenpo.challenge.model.dto.PercentageResponse;
import com.tenpo.challenge.model.mapper.CalculationMapper;
import com.tenpo.challenge.service.impl.CalculationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalculationServiceTest {

    @Mock
    private PercentageService percentageService;

    @Mock
    private CalculationMapper calculationMapper;

    @InjectMocks
    private CalculationServiceImpl calculationService;

    private CalculationRequest request;

    @BeforeEach
    void setUp() {
        request = new CalculationRequest(100.0, 50.0);
    }

    @Test
    void shouldCalculateCorrectlyWhenPercentageIsAvailable() {
        when(percentageService.getPercentage()).thenReturn(new PercentageResponse(10.0, "EXTERNAL_API"));
        when(calculationMapper.toResponse(any(), eq(10.0), eq(165.0)))
                .thenReturn(new CalculationResponse(100.0, 50.0, 10.0, 165.0));

        CalculationResponse response = calculationService.calculate(request);

        assertEquals(100.0, response.getFirstNumber());
        assertEquals(50.0, response.getSecondNumber());
        assertEquals(10.0, response.getPercentage());
        assertEquals(165.0, response.getResult());

        verify(percentageService, times(1)).getPercentage();
    }

    @Test
    void shouldHandleZeroPercentageCorrectly() {
        when(percentageService.getPercentage()).thenReturn(new PercentageResponse(0.0, "EXTERNAL_API"));
        when(calculationMapper.toResponse(any(), eq(0.0), eq(150.0)))
                .thenReturn(new CalculationResponse(100.0, 50.0, 0.0, 150.0));

        CalculationResponse response = calculationService.calculate(request);

        assertEquals(150.0, response.getResult());

        verify(percentageService, times(1)).getPercentage();
    }

    @Test
    void shouldHandleHighPercentageCorrectly() {
        when(percentageService.getPercentage()).thenReturn(new PercentageResponse(200.0, "EXTERNAL_API"));
        when(calculationMapper.toResponse(any(), eq(200.0), eq(450.0)))
                .thenReturn(new CalculationResponse(100.0, 50.0, 200.0, 450.0));

        CalculationResponse response = calculationService.calculate(request);

        assertEquals(450.0, response.getResult());

        verify(percentageService, times(1)).getPercentage();
    }
}
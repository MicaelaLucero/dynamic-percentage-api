package com.tenpo.challenge.service.impl;

import com.tenpo.challenge.dto.CalculationRequest;
import com.tenpo.challenge.dto.CalculationResponse;
import com.tenpo.challenge.service.CalculationService;
import com.tenpo.challenge.service.PercentageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CalculationServiceImpl implements CalculationService {

    private final PercentageService percentageService;

    public CalculationServiceImpl(PercentageService percentageService) {
        this.percentageService = percentageService;
    }

    @Override
    public CalculationResponse calculate(CalculationRequest request) {
        double sum = request.getFirstNumber() + request.getSecondNumber();
        double percentage = percentageService.getPercentage() != null ? percentageService.getPercentage() : 0.0;

        log.info("Percentage used in calculation: {}", percentage);
        double result =  sum + (sum * (percentage / 100));

        return CalculationResponse.builder()
                .firstNumber(request.getFirstNumber())
                .secondNumber(request.getSecondNumber())
                .result(result)
                .build();
    }
}
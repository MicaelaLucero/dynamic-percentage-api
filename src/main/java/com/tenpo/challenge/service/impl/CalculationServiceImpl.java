package com.tenpo.challenge.service.impl;

import com.tenpo.challenge.model.dto.CalculationRequest;
import com.tenpo.challenge.model.dto.CalculationResponse;
import com.tenpo.challenge.model.dto.PercentageResponse;
import com.tenpo.challenge.model.mapper.CalculationMapper;
import com.tenpo.challenge.service.CalculationService;
import com.tenpo.challenge.service.PercentageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CalculationServiceImpl implements CalculationService {

    private final PercentageService percentageService;
    private final CalculationMapper calculationMapper;

    @Override
    public CalculationResponse calculate(CalculationRequest request) {
        double sum = request.getFirstNumber() + request.getSecondNumber();

        PercentageResponse percentageResponse = percentageService.getPercentage();
        double percentage = percentageResponse.getPercentage();

        log.info("Percentage used in calculation: {}", percentage);
        double result =  sum + (sum * (percentage / 100));

        return calculationMapper.toResponse(request, percentage, result);
    }
}
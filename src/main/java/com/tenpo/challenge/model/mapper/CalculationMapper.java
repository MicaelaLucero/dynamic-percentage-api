package com.tenpo.challenge.model.mapper;

import com.tenpo.challenge.model.dto.CalculationRequest;
import com.tenpo.challenge.model.dto.CalculationResponse;
import org.springframework.stereotype.Component;

@Component
public class CalculationMapper {

    public CalculationResponse toResponse(CalculationRequest request, double percentage, double result) {
        return CalculationResponse.builder()
                .firstNumber(request.getFirstNumber())
                .secondNumber(request.getSecondNumber())
                .percentage(percentage)
                .result(result)
                .build();
    }
}

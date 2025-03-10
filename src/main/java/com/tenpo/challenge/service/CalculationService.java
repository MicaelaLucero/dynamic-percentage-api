package com.tenpo.challenge.service;

import com.tenpo.challenge.model.dto.CalculationRequest;
import com.tenpo.challenge.model.dto.CalculationResponse;

public interface CalculationService {
    CalculationResponse calculate(CalculationRequest request);
}

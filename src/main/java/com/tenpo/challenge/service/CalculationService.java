package com.tenpo.challenge.service;

import com.tenpo.challenge.dto.CalculationRequest;
import com.tenpo.challenge.dto.CalculationResponse;

public interface CalculationService {
    CalculationResponse calculate(CalculationRequest request);
}

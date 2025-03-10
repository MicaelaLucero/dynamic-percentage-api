package com.tenpo.challenge.service;

import com.tenpo.challenge.model.dto.PercentageResponse;
import com.tenpo.challenge.model.dto.UpdatePercentageResponse;

public interface PercentageService {
    PercentageResponse getPercentage();
    UpdatePercentageResponse updatePercentage(Double newPercentage);
}

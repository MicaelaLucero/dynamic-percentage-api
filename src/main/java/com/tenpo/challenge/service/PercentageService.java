package com.tenpo.challenge.service;

import com.tenpo.challenge.dto.PercentageResponse;
import com.tenpo.challenge.dto.UpdatePercentageResponse;

public interface PercentageService {
    PercentageResponse getPercentage();
    UpdatePercentageResponse updatePercentage(Double newPercentage);
}

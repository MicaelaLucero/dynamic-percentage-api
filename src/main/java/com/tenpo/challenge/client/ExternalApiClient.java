package com.tenpo.challenge.client;

import com.tenpo.challenge.dto.UpdatePercentageResponse;

public interface ExternalApiClient {
    Double getPercentage();
    UpdatePercentageResponse updatePercentage(Double newPercentage);
}

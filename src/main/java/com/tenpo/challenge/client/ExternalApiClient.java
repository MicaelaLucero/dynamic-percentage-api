package com.tenpo.challenge.client;

public interface ExternalApiClient {
    Double getPercentage();
    String updatePercentage(Double newPercentage);
}

package com.tenpo.challenge.utils;

public class Constants {
    public static final String EXTERNAL_API_URL = "http://wiremock:8080/external-percentage";
    public static final String WIREMOCK_ADMIN_URL = "http://wiremock:8080/__admin";

    public static final String CACHE_KEY = "percentage:latest";
    public static final long CACHE_EXPIRATION_SECONDS = 30;

    public static final String CACHE = "CACHE";
    public static final String EXTERNAL_API = "EXTERNAL_API";
}

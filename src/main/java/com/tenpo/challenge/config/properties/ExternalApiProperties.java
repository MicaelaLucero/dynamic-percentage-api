package com.tenpo.challenge.config.properties;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "external.api")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExternalApiProperties {
    private String wiremockUrl;
    private String wiremockAdminUrl;
}
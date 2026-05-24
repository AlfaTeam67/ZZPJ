package com.fininsight.marketdata.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Externalized configuration for the Finnhub REST API.
 * Values are supplied by Spring Cloud Config (market-data-service.yml).
 *
 * <p>The {@code api-key} must never be hard-coded — it must be injected
 * via the {@code FINNHUB_API_KEY} environment variable.</p>
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "finnhub")
public class FinnhubProperties {

    /**
     * Finnhub REST API base URL (e.g. {@code https://finnhub.io/api/v1}).
     */
    @NotBlank
    private String baseUrl = "https://finnhub.io/api/v1";

    /**
     * API key obtained from <a href="https://finnhub.io">finnhub.io</a>.
     * Injected from env-var {@code FINNHUB_API_KEY}.
     */
    @NotBlank
    private String apiKey;
}

package com.fininsight.marketdata.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Central configuration for the market-data integration layer:
 * <ul>
 *   <li>Registers {@link FinnhubProperties} and {@link MarketDataSchedulerProperties}</li>
 *   <li>Provides an external {@link RestClient.Builder} (no Eureka load-balancing) for
 *       outbound calls to Finnhub — consistent with the pattern in {@code ai-advisor-service}</li>
 *   <li>Enables Spring's {@code @Scheduled} task executor</li>
 * </ul>
 */
@Configuration
@EnableScheduling
@EnableConfigurationProperties({FinnhubProperties.class, MarketDataSchedulerProperties.class})
public class MarketDataConfig {

    /**
     * Prototype-scoped builder for external (non-Eureka) HTTP calls.
     * Connect timeout 5 s, read timeout 10 s — appropriate for a public market-data API.
     */
    @Bean
    @Scope("prototype")
    public RestClient.Builder externalRestClientBuilder() {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        return RestClient.builder().requestFactory(factory);
    }
}

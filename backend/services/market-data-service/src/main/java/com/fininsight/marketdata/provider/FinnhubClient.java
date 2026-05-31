package com.fininsight.marketdata.provider;

import com.fininsight.marketdata.config.FinnhubProperties;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Finnhub REST API adapter.
 *
 * <p>Uses Spring Boot 3.2's {@link RestClient} — identical pattern to
 * {@code ai-advisor-service}'s {@code FinnhubNewsClient}.</p>
 *
 * <p>Rate-limit and transient errors are retried by the Resilience4j
 * {@code @Retry("finnhub-fetch")} aspect, configured in
 * {@code market-data-service.yml}.</p>
 */
@Component
@Slf4j
public class FinnhubClient implements MarketDataProvider {

    private static final String PROVIDER_NAME = "finnhub";

    private final RestClient restClient;
    private final String apiKey;

    public FinnhubClient(RestClient.Builder externalRestClientBuilder,
                         FinnhubProperties properties) {
        this.restClient = externalRestClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build();
        // Store the key locally — never log it
        this.apiKey = properties.getApiKey();
    }

    /**
     * Fetches real-time quote from {@code GET /quote?symbol={symbol}&token={key}}.
     *
     * <p>HTTP 429 → {@link FinnhubRateLimitException} (retried by Resilience4j).
     * Empty/zero quote → {@link FinnhubSymbolNotFoundException} (not retried).</p>
     */
    @Override
    @Retry(name = "finnhub-fetch")
    public FinnhubQuoteResponse fetchQuote(String symbol) {
        log.debug("Fetching Finnhub quote for symbol: {}", symbol);

        FinnhubQuoteResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/quote")
                        .queryParam("symbol", symbol)
                        .queryParam("token", apiKey)
                        .build())
                .retrieve()
                .onStatus(status -> status == HttpStatus.TOO_MANY_REQUESTS,
                        (req, res) -> {
                            log.warn("Finnhub rate-limit hit for symbol: {}", symbol);
                            throw new FinnhubRateLimitException(symbol);
                        })
                .onStatus(HttpStatusCode::is5xxServerError,
                        (req, res) -> {
                            log.warn("Finnhub 5xx error for symbol: {}, status: {}",
                                    symbol, res.getStatusCode());
                            throw new org.springframework.web.client.HttpServerErrorException(
                                    res.getStatusCode());
                        })
                .body(FinnhubQuoteResponse.class);

        if (response == null || response.isEmpty()) {
            log.debug("Finnhub returned no data for symbol: {}", symbol);
            throw new FinnhubSymbolNotFoundException(symbol);
        }

        log.debug("Finnhub quote received for symbol: {}, price: {}", symbol, response.getCurrentPrice());
        return response;
    }

    @Override
    public String providerName() {
        return PROVIDER_NAME;
    }

    @Override
    @Retry(name = "finnhub-fetch")
    public FinnhubSearchResponse searchSymbols(String query) {
        log.debug("Fetching Finnhub search for query: {}", query);

        FinnhubSearchResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("q", query)
                        .queryParam("token", apiKey)
                        .build())
                .retrieve()
                .onStatus(status -> status == HttpStatus.TOO_MANY_REQUESTS,
                        (req, res) -> {
                            log.warn("Finnhub rate-limit hit for search query: {}", query);
                            throw new FinnhubRateLimitException(query);
                        })
                .onStatus(HttpStatusCode::is5xxServerError,
                        (req, res) -> {
                            log.warn("Finnhub 5xx error for search query: {}, status: {}",
                                    query, res.getStatusCode());
                            throw new org.springframework.web.client.HttpServerErrorException(
                                    res.getStatusCode());
                        })
                .body(FinnhubSearchResponse.class);

        return response;
    }
}

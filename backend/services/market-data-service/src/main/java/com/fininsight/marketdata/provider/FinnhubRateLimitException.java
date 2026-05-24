package com.fininsight.marketdata.provider;

/**
 * Thrown when Finnhub returns HTTP 429 (Too Many Requests).
 * Resilience4j retry is configured to retry on this exception.
 */
public class FinnhubRateLimitException extends RuntimeException {

    public FinnhubRateLimitException(String symbol) {
        super("Finnhub rate-limit exceeded while fetching symbol: " + symbol);
    }
}

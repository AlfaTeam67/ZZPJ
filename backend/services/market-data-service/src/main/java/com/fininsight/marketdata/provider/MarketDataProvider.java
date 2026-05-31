package com.fininsight.marketdata.provider;

/**
 * Abstraction over external market-data feed providers.
 * Add further implementations (e.g. AlphaVantage) without touching the scheduler.
 */
public interface MarketDataProvider {

    /**
     * Fetches the current quote for the given ticker symbol.
     *
     * @param symbol ticker (e.g. {@code AAPL}, {@code BTC-USD})
     * @return populated quote, never {@code null}
     * @throws FinnhubRateLimitException    when the provider enforces a rate limit (HTTP 429)
     * @throws FinnhubSymbolNotFoundException when the provider has no data for {@code symbol}
     * @throws org.springframework.web.client.ResourceAccessException on network errors
     * @throws org.springframework.web.client.HttpServerErrorException on 5xx responses
     */
    FinnhubQuoteResponse fetchQuote(String symbol);

    /**
     * Searches for symbols by company name or ticker.
     */
    FinnhubSearchResponse searchSymbols(String query);

    /**
     * Human-readable provider name used as the {@code source} field in {@code PriceSnapshot}.
     */
    String providerName();
}

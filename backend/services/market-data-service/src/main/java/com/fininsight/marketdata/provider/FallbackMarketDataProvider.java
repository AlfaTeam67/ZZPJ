package com.fininsight.marketdata.provider;

import com.fininsight.marketdata.exception.MarketDataUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class FallbackMarketDataProvider implements MarketDataProvider {

    private final FinnhubClient finnhubClient;

    @Override
    @CircuitBreaker(name = "finnhub-circuit", fallbackMethod = "fallbackQuote")
    public FinnhubQuoteResponse fetchQuote(String symbol) {
        return finnhubClient.fetchQuote(symbol);
    }

    FinnhubQuoteResponse fallbackQuote(String symbol, Throwable t) {
        log.warn("finnhub provider unavailable for symbol: {}. Reason: {}", symbol, t.getMessage());
        throw new MarketDataUnavailableException("Market data unavailable for: " + symbol, t);
    }

    @Override
    public String providerName() {
        return finnhubClient.providerName();
    }
}

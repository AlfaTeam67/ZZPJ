package com.fininsight.marketdata.provider;

/**
 * Thrown when Finnhub returns an empty quote for a symbol (price == 0 with no data).
 * Resilience4j retry is configured to <em>ignore</em> this exception — retrying a
 * missing symbol would be futile.
 */
public class FinnhubSymbolNotFoundException extends RuntimeException {

    public FinnhubSymbolNotFoundException(String symbol) {
        super("Finnhub returned no data for symbol: " + symbol);
    }
}

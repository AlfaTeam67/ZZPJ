package com.fininsight.marketdata.exception;

public class MarketDataUnavailableException extends RuntimeException {

    public MarketDataUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

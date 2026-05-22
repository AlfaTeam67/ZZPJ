package com.fininsight.advisor.exception;

public class PortfolioNotAvailableException extends RuntimeException {
    public PortfolioNotAvailableException(String message) {
        super(message);
    }

    public PortfolioNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

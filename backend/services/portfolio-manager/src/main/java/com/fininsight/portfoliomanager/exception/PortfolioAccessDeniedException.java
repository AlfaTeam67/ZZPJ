package com.fininsight.portfoliomanager.exception;

public class PortfolioAccessDeniedException extends RuntimeException {
    public PortfolioAccessDeniedException(String message) {
        super(message);
    }
}

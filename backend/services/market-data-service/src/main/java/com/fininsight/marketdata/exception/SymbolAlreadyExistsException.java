package com.fininsight.marketdata.exception;

public class SymbolAlreadyExistsException extends RuntimeException {
    public SymbolAlreadyExistsException(String symbol) {
        super("Symbol already exists: " + symbol);
    }
}

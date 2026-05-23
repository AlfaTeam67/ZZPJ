package com.fininsight.marketdata.exception;

public class SymbolNotFoundException extends RuntimeException {
    public SymbolNotFoundException(String symbol) {
        super("Symbol not found: " + symbol);
    }
}

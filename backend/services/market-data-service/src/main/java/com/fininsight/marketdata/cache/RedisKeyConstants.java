package com.fininsight.marketdata.cache;

public final class RedisKeyConstants {
    
    private RedisKeyConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
    
    public static final String PRICE_KEY_PREFIX = "price:";
    public static final String SYMBOLS_ALL_KEY = "symbols:all";
    
    public static String priceKey(String symbol) {
        return PRICE_KEY_PREFIX + symbol;
    }
}

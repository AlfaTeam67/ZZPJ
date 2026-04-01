package com.fininsight.marketdata.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PriceCacheService {
    
    private static final Duration PRICE_TTL = Duration.ofSeconds(60);
    private static final Duration SYMBOLS_TTL = Duration.ofHours(1);
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    public void cachePrice(PriceCacheEntry entry) {
        try {
            String key = RedisKeyConstants.priceKey(entry.symbol());
            redisTemplate.opsForValue().set(key, entry, PRICE_TTL);
            log.debug("Cached price for symbol: {}", entry.symbol());
        } catch (Exception e) {
            log.error("Failed to cache price for symbol: {}", entry.symbol(), e);
        }
    }
    
    public Optional<PriceCacheEntry> getPrice(String symbol) {
        try {
            String key = RedisKeyConstants.priceKey(symbol);
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return Optional.empty();
            }
            
            if (value instanceof PriceCacheEntry) {
                return Optional.of((PriceCacheEntry) value);
            }
            
            String json = objectMapper.writeValueAsString(value);
            PriceCacheEntry entry = objectMapper.readValue(json, PriceCacheEntry.class);
            return Optional.of(entry);
        } catch (Exception e) {
            log.error("Failed to get cached price for symbol: {}", symbol, e);
            return Optional.empty();
        }
    }
    
    public void cacheSymbolList(List<String> symbols) {
        try {
            redisTemplate.opsForValue().set(
                RedisKeyConstants.SYMBOLS_ALL_KEY, 
                symbols, 
                SYMBOLS_TTL
            );
            log.debug("Cached {} symbols", symbols.size());
        } catch (Exception e) {
            log.error("Failed to cache symbol list", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    public Optional<List<String>> getSymbolList() {
        try {
            Object value = redisTemplate.opsForValue().get(RedisKeyConstants.SYMBOLS_ALL_KEY);
            if (value instanceof List) {
                return Optional.of((List<String>) value);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to get cached symbol list", e);
            return Optional.empty();
        }
    }
    
    public void evictPrice(String symbol) {
        try {
            String key = RedisKeyConstants.priceKey(symbol);
            redisTemplate.delete(key);
            log.debug("Evicted cached price for symbol: {}", symbol);
        } catch (Exception e) {
            log.error("Failed to evict price for symbol: {}", symbol, e);
        }
    }
    
    public void evictAllSymbols() {
        try {
            redisTemplate.delete(RedisKeyConstants.SYMBOLS_ALL_KEY);
            log.debug("Evicted cached symbol list");
        } catch (Exception e) {
            log.error("Failed to evict symbol list", e);
        }
    }
}

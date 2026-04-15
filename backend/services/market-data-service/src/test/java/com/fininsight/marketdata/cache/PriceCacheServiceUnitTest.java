package com.fininsight.marketdata.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceCacheServiceUnitTest {
    
    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @Mock
    private ValueOperations<String, Object> valueOperations;
    
    @InjectMocks
    private PriceCacheService cacheService;
    
    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }
    
    @Test
    void shouldCachePriceSuccessfully() {
        PriceCacheEntry entry = new PriceCacheEntry(
                "AAPL",
                new BigDecimal("150.25"),
                "USD",
                new BigDecimal("2.5"),
                Instant.now()
        );
        
        cacheService.cachePrice(entry);
        
        verify(valueOperations).set(
                eq("price:AAPL"),
                eq(entry),
                eq(Duration.ofSeconds(60))
        );
    }
    
    @Test
    void shouldHandleCachePriceError() {
        PriceCacheEntry entry = new PriceCacheEntry(
                "BTC-USD",
                new BigDecimal("50000"),
                "USD",
                null,
                Instant.now()
        );
        
        doThrow(new RuntimeException("Redis error")).when(valueOperations)
                .set(anyString(), any(), any(Duration.class));
        
        // Should not throw - graceful degradation
        cacheService.cachePrice(entry);
        
        verify(valueOperations).set(anyString(), eq(entry), any(Duration.class));
    }
    
    @Test
    void shouldGetPriceFromCache() {
        PriceCacheEntry expected = new PriceCacheEntry(
                "AAPL",
                new BigDecimal("150.25"),
                "USD",
                new BigDecimal("2.5"),
                Instant.now()
        );
        
        when(valueOperations.get("price:AAPL")).thenReturn(expected);
        
        Optional<PriceCacheEntry> result = cacheService.getPrice("AAPL");
        
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expected);
        verify(valueOperations).get("price:AAPL");
    }
    
    @Test
    void shouldReturnEmptyWhenPriceNotInCache() {
        when(valueOperations.get("price:NONEXISTENT")).thenReturn(null);
        
        Optional<PriceCacheEntry> result = cacheService.getPrice("NONEXISTENT");
        
        assertThat(result).isEmpty();
        verify(valueOperations).get("price:NONEXISTENT");
    }
    
    @Test
    void shouldHandleGetPriceError() {
        when(valueOperations.get(anyString())).thenThrow(new RuntimeException("Redis error"));
        
        Optional<PriceCacheEntry> result = cacheService.getPrice("AAPL");
        
        assertThat(result).isEmpty();
        verify(valueOperations).get("price:AAPL");
    }
    
    @Test
    void shouldDeserializeObjectWhenNotDirectType() {
        Object rawValue = new Object();
        PriceCacheEntry expected = new PriceCacheEntry(
                "AAPL",
                new BigDecimal("150.25"),
                "USD",
                new BigDecimal("2.5"),
                Instant.parse("2026-03-31T10:00:00Z")
        );
        
        when(valueOperations.get("price:AAPL")).thenReturn(rawValue);
        when(objectMapper.convertValue(rawValue, PriceCacheEntry.class)).thenReturn(expected);
        
        Optional<PriceCacheEntry> result = cacheService.getPrice("AAPL");
        
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(expected);
        verify(objectMapper).convertValue(rawValue, PriceCacheEntry.class);
    }
    
    @Test
    void shouldCacheSymbolList() {
        List<String> symbols = List.of("AAPL", "GOOGL", "MSFT");
        
        cacheService.cacheSymbolList(symbols);
        
        verify(valueOperations).set(
                eq("symbols:all"),
                eq(symbols),
                eq(Duration.ofHours(1))
        );
    }
    
    @Test
    void shouldGetSymbolListFromCache() {
        List<String> expected = List.of("AAPL", "GOOGL", "MSFT");
        when(valueOperations.get("symbols:all")).thenReturn(expected);
        
        Optional<List<String>> result = cacheService.getSymbolList();
        
        assertThat(result).isPresent();
        assertThat(result.get()).containsExactlyInAnyOrder("AAPL", "GOOGL", "MSFT");
        verify(valueOperations).get("symbols:all");
    }
    
    @Test
    void shouldReturnEmptyWhenSymbolListNotInCache() {
        when(valueOperations.get("symbols:all")).thenReturn(null);
        
        Optional<List<String>> result = cacheService.getSymbolList();
        
        assertThat(result).isEmpty();
    }
    
    @Test
    void shouldEvictPrice() {
        cacheService.evictPrice("AAPL");
        
        verify(redisTemplate).delete("price:AAPL");
    }
    
    @Test
    void shouldHandleEvictPriceError() {
        when(redisTemplate.delete(anyString())).thenThrow(new RuntimeException("Redis error"));
        
        // Should not throw - graceful degradation
        cacheService.evictPrice("AAPL");
        
        verify(redisTemplate).delete("price:AAPL");
    }
    
    @Test
    void shouldEvictAllSymbols() {
        cacheService.evictAllSymbols();
        
        verify(redisTemplate).delete("symbols:all");
    }
    
    @Test
    void shouldUseCorrectTTLForPrices() {
        PriceCacheEntry entry = new PriceCacheEntry(
                "TEST",
                BigDecimal.ONE,
                "USD",
                null,
                Instant.now()
        );
        
        cacheService.cachePrice(entry);
        
        verify(valueOperations).set(
                anyString(),
                any(),
                eq(Duration.ofSeconds(60))
        );
    }
    
    @Test
    void shouldUseCorrectTTLForSymbols() {
        List<String> symbols = List.of("TEST");
        
        cacheService.cacheSymbolList(symbols);
        
        verify(valueOperations).set(
                anyString(),
                any(),
                eq(Duration.ofHours(1))
        );
    }
    
    @Test
    void shouldUseCorrectKeyPattern() {
        PriceCacheEntry entry = new PriceCacheEntry(
                "BTC-USD",
                BigDecimal.ONE,
                "USD",
                null,
                Instant.now()
        );
        
        cacheService.cachePrice(entry);
        
        verify(valueOperations).set(
                eq("price:BTC-USD"),
                any(),
                any(Duration.class)
        );
    }
}

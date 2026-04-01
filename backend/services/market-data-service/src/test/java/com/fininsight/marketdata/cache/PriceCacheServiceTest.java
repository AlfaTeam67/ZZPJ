package com.fininsight.marketdata.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.SECONDS;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class PriceCacheServiceTest {
    
    private static final DockerImageName REDIS_IMAGE = DockerImageName.parse("redis:7-alpine");
    
    @Container
    private static final GenericContainer<?> redisContainer = new GenericContainer<>(REDIS_IMAGE)
            .withExposedPorts(6379);
    
    @DynamicPropertySource
    static void overrideRedisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
    }
    
    @Autowired
    private PriceCacheService cacheService;
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }
    
    @Test
    void shouldCacheAndRetrievePrice() {
        PriceCacheEntry entry = new PriceCacheEntry(
                "AAPL",
                new BigDecimal("150.25"),
                "USD",
                new BigDecimal("2.5"),
                Instant.now()
        );
        
        cacheService.cachePrice(entry);
        
        Optional<PriceCacheEntry> cached = cacheService.getPrice("AAPL");
        
        assertThat(cached).isPresent();
        assertThat(cached.get().symbol()).isEqualTo("AAPL");
        assertThat(cached.get().price()).isEqualByComparingTo(new BigDecimal("150.25"));
    }
    
    @Test
    void shouldReturnEmptyForNonExistentPrice() {
        Optional<PriceCacheEntry> cached = cacheService.getPrice("NONEXISTENT");
        
        assertThat(cached).isEmpty();
    }
    
    @Test
    void shouldCacheAndRetrieveSymbolList() {
        List<String> symbols = List.of("AAPL", "GOOGL", "MSFT");
        
        cacheService.cacheSymbolList(symbols);
        
        Optional<List<String>> cached = cacheService.getSymbolList();
        
        assertThat(cached).isPresent();
        assertThat(cached.get()).containsExactlyInAnyOrder("AAPL", "GOOGL", "MSFT");
    }
    
    @Test
    void shouldEvictPrice() {
        PriceCacheEntry entry = new PriceCacheEntry(
                "BTC-USD",
                new BigDecimal("50000.00"),
                "USD",
                null,
                Instant.now()
        );
        
        cacheService.cachePrice(entry);
        assertThat(cacheService.getPrice("BTC-USD")).isPresent();
        
        cacheService.evictPrice("BTC-USD");
        
        assertThat(cacheService.getPrice("BTC-USD")).isEmpty();
    }
    
    @Test
    void shouldEvictSymbolList() {
        List<String> symbols = List.of("EUR", "GBP", "JPY");
        
        cacheService.cacheSymbolList(symbols);
        assertThat(cacheService.getSymbolList()).isPresent();
        
        cacheService.evictAllSymbols();
        
        assertThat(cacheService.getSymbolList()).isEmpty();
    }
    
    @Test
    void shouldExpirePriceAfterTTL() {
        PriceCacheEntry entry = new PriceCacheEntry(
                "ETH-USD",
                new BigDecimal("3000.00"),
                "USD",
                new BigDecimal("5.0"),
                Instant.now()
        );
        
        cacheService.cachePrice(entry);
        assertThat(cacheService.getPrice("ETH-USD")).isPresent();
        
        await().atMost(65, SECONDS)
                .pollInterval(5, SECONDS)
                .untilAsserted(() -> 
                    assertThat(cacheService.getPrice("ETH-USD")).isEmpty()
                );
    }
    
    @Test
    void shouldHandleBigDecimalInCache() {
        PriceCacheEntry entry = new PriceCacheEntry(
                "DOGE",
                new BigDecimal("0.000123"),
                "USD",
                new BigDecimal("-10.5678"),
                Instant.now()
        );
        
        cacheService.cachePrice(entry);
        
        Optional<PriceCacheEntry> cached = cacheService.getPrice("DOGE");
        
        assertThat(cached).isPresent();
        assertThat(cached.get().price()).isEqualByComparingTo(new BigDecimal("0.000123"));
        assertThat(cached.get().changePct24h()).isEqualByComparingTo(new BigDecimal("-10.5678"));
    }
}

package com.fininsight.marketdata.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import com.fininsight.marketdata.entity.PriceSnapshot;
import com.fininsight.marketdata.entity.SupportedSymbol;
import com.fininsight.marketdata.entity.enums.SymbolType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class PriceSnapshotRepositoryTest {
    
    @Autowired
    private PriceSnapshotRepository priceRepository;
    
    @Autowired
    private SupportedSymbolRepository symbolRepository;
    
    private SupportedSymbol testSymbol;
    
    @BeforeEach
    void setUp() {
        testSymbol = SupportedSymbol.builder()
                .symbol("AAPL")
                .type(SymbolType.STOCK)
                .apiSource("alphavantage")
                .active(true)
                .build();
        symbolRepository.saveAndFlush(testSymbol);
    }
    
    @Test
    void shouldSaveAndFindPriceSnapshot() {
        PriceSnapshot snapshot = PriceSnapshot.builder()
                .symbol(testSymbol)
                .source("alphavantage")
                .price(new BigDecimal("150.25"))
                .currency("USD")
                .changePct24h(new BigDecimal("2.5"))
                .volume24h(new BigDecimal("1000000"))
                .fetchedAt(Instant.now())
                .build();
        
        PriceSnapshot saved = priceRepository.save(snapshot);
        
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPrice()).isEqualByComparingTo(new BigDecimal("150.25"));
    }
    
    @Test
    void shouldFindLatestPriceBySymbol() {
        Instant now = Instant.now();
        
        PriceSnapshot older = PriceSnapshot.builder()
                .symbol(testSymbol)
                .source("test")
                .price(new BigDecimal("100.00"))
                .currency("USD")
                .fetchedAt(now.minusSeconds(3600))
                .build();
        
        PriceSnapshot newer = PriceSnapshot.builder()
                .symbol(testSymbol)
                .source("test")
                .price(new BigDecimal("150.00"))
                .currency("USD")
                .fetchedAt(now)
                .build();
        
        priceRepository.save(older);
        priceRepository.save(newer);
        
        Optional<PriceSnapshot> latest = priceRepository.findTopBySymbolOrderByFetchedAtDesc(testSymbol);
        
        assertThat(latest).isPresent();
        assertThat(latest.get().getPrice()).isEqualByComparingTo(new BigDecimal("150.00"));
    }
    
    @Test
    void shouldFindLatestBySymbolString() {
        PriceSnapshot snapshot = PriceSnapshot.builder()
                .symbol(testSymbol)
                .source("test")
                .price(new BigDecimal("200.00"))
                .currency("USD")
                .fetchedAt(Instant.now())
                .build();
        
        priceRepository.save(snapshot);
        
        Optional<PriceSnapshot> found = priceRepository.findLatestBySymbolString("AAPL");
        
        assertThat(found).isPresent();
        assertThat(found.get().getSymbol().getSymbol()).isEqualTo("AAPL");
    }
    
    @Test
    void shouldHandleBigDecimalPrecision() {
        PriceSnapshot snapshot = PriceSnapshot.builder()
                .symbol(testSymbol)
                .source("test")
                .price(new BigDecimal("0.0001"))
                .currency("BTC")
                .changePct24h(new BigDecimal("-1.2345"))
                .volume24h(new BigDecimal("999999999999.1234"))
                .fetchedAt(Instant.now())
                .build();
        
        PriceSnapshot saved = priceRepository.save(snapshot);
        
        assertThat(saved.getPrice()).isEqualByComparingTo(new BigDecimal("0.0001"));
        assertThat(saved.getChangePct24h()).isEqualByComparingTo(new BigDecimal("-1.2345"));
    }
}

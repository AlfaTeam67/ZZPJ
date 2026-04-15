package com.fininsight.marketdata.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import com.fininsight.marketdata.entity.SupportedSymbol;
import com.fininsight.marketdata.entity.enums.SymbolType;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SupportedSymbolRepositoryTest {
    
    @Autowired
    private SupportedSymbolRepository repository;
    
    @Test
    void shouldSaveAndFindSymbol() {
        SupportedSymbol symbol = SupportedSymbol.builder()
                .symbol("AAPL")
                .type(SymbolType.STOCK)
                .apiSource("alphavantage")
                .active(true)
                .baseCurrency("USD")
                .addedAt(Instant.now())
                .build();
        
        SupportedSymbol saved = repository.save(symbol);
        
        assertThat(saved.getSymbol()).isEqualTo("AAPL");
        assertThat(saved.getType()).isEqualTo(SymbolType.STOCK);
    }
    
    @Test
    void shouldFindActiveSymbols() {
        SupportedSymbol active = SupportedSymbol.builder()
                .symbol("BTC-USD")
                .type(SymbolType.CRYPTO)
                .apiSource("coingecko")
                .active(true)
                .build();
        
        SupportedSymbol inactive = SupportedSymbol.builder()
                .symbol("ETH-USD")
                .type(SymbolType.CRYPTO)
                .apiSource("coingecko")
                .active(false)
                .build();
        
        repository.saveAll(List.of(active, inactive));
        
        List<SupportedSymbol> activeSymbols = repository.findByActiveTrue();
        
        assertThat(activeSymbols).hasSize(1);
        assertThat(activeSymbols.get(0).getSymbol()).isEqualTo("BTC-USD");
    }
    
    @Test
    void shouldFindBySymbolAndActive() {
        SupportedSymbol symbol = SupportedSymbol.builder()
                .symbol("EUR")
                .type(SymbolType.FOREX)
                .apiSource("nbp")
                .active(true)
                .baseCurrency("PLN")
                .build();
        
        repository.save(symbol);
        
        Optional<SupportedSymbol> found = repository.findBySymbolAndActiveTrue("EUR");
        
        assertThat(found).isPresent();
        assertThat(found.get().getType()).isEqualTo(SymbolType.FOREX);
    }
}

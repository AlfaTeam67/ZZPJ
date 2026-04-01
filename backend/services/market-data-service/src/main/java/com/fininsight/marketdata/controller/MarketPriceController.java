package com.fininsight.marketdata.controller;

import com.fininsight.marketdata.entity.PriceSnapshot;
import com.fininsight.marketdata.entity.SupportedSymbol;
import com.fininsight.marketdata.dto.MarketPriceDto;
import com.fininsight.marketdata.repository.PriceSnapshotRepository;
import com.fininsight.marketdata.repository.SupportedSymbolRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/market-prices")
@RequiredArgsConstructor
@Tag(name = "Market Prices", description = "Market price data API")
@SecurityRequirement(name = "bearerAuth")
public class MarketPriceController {
    
    private final PriceSnapshotRepository marketPriceRepository;
    private final SupportedSymbolRepository symbolRepository;
    
    @GetMapping("/latest")
    @Operation(summary = "Get latest prices for all symbols")
    public ResponseEntity<List<MarketPriceDto>> getLatestPrices() {
        List<MarketPriceDto> prices = marketPriceRepository.findLatestSnapshotsForActiveSymbols().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(prices);
    }
    
    @GetMapping("/symbol/{ticker}")
    @Operation(summary = "Get prices for a specific symbol")
    public ResponseEntity<List<MarketPriceDto>> getPricesBySymbol(@PathVariable String ticker) {
        return symbolRepository.findBySymbolAndActiveTrue(ticker)
            .map(symbol -> {
                List<MarketPriceDto> prices = marketPriceRepository
                    .findBySymbolOrderByFetchedAtDesc(symbol).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
                return ResponseEntity.ok(prices);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Add new market price")
    public ResponseEntity<MarketPriceDto> addMarketPrice(@Valid @RequestBody MarketPriceDto priceDto) {
        return symbolRepository.findBySymbolAndActiveTrue(priceDto.getSymbol())
            .map(symbol -> {
                PriceSnapshot marketPrice = PriceSnapshot.builder()
                    .symbol(symbol)
                    .source(priceDto.getSource())
                    .price(priceDto.getPrice())
                    .currency(priceDto.getCurrency())
                    .changePct24h(priceDto.getChangePct24h())
                    .volume24h(priceDto.getVolume24h())
                    .fetchedAt(priceDto.getFetchedAt() != null ? priceDto.getFetchedAt() : Instant.now())
                    .build();
                
                PriceSnapshot saved = marketPriceRepository.save(marketPrice);
                return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
            })
            .orElse(ResponseEntity.badRequest().build());
    }
    
    private MarketPriceDto toDto(PriceSnapshot marketPrice) {
        return MarketPriceDto.builder()
            .id(marketPrice.getId())
            .symbol(marketPrice.getSymbol().getSymbol())
            .source(marketPrice.getSource())
            .price(marketPrice.getPrice())
            .currency(marketPrice.getCurrency())
            .changePct24h(marketPrice.getChangePct24h())
            .volume24h(marketPrice.getVolume24h())
            .fetchedAt(marketPrice.getFetchedAt())
            .build();
    }
}

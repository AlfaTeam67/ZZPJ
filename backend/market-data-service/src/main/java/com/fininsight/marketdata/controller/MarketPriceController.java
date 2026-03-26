package com.fininsight.marketdata.controller;

import com.fininsight.marketdata.dto.MarketPriceDto;
import com.fininsight.marketdata.entity.MarketPrice;
import com.fininsight.marketdata.entity.Symbol;
import com.fininsight.marketdata.repository.MarketPriceRepository;
import com.fininsight.marketdata.repository.SymbolRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/market-prices")
@RequiredArgsConstructor
@Tag(name = "Market Prices", description = "Market price data API")
@SecurityRequirement(name = "bearerAuth")
public class MarketPriceController {
    
    private final MarketPriceRepository marketPriceRepository;
    private final SymbolRepository symbolRepository;
    
    @GetMapping("/latest")
    @Operation(summary = "Get latest prices for all symbols")
    public ResponseEntity<List<MarketPriceDto>> getLatestPrices() {
        List<MarketPriceDto> prices = marketPriceRepository.findLatestPrices().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(prices);
    }
    
    @GetMapping("/symbol/{ticker}")
    @Operation(summary = "Get prices for a specific symbol")
    public ResponseEntity<List<MarketPriceDto>> getPricesBySymbol(@PathVariable String ticker) {
        return symbolRepository.findByTicker(ticker)
            .map(symbol -> {
                List<MarketPriceDto> prices = marketPriceRepository
                    .findBySymbolOrderByTimestampDesc(symbol).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
                return ResponseEntity.ok(prices);
            })
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Add new market price")
    public ResponseEntity<MarketPriceDto> addMarketPrice(@Valid @RequestBody MarketPriceDto priceDto) {
        return symbolRepository.findByTicker(priceDto.getTicker())
            .map(symbol -> {
                MarketPrice marketPrice = MarketPrice.builder()
                    .symbol(symbol)
                    .price(priceDto.getPrice())
                    .volume(priceDto.getVolume())
                    .timestamp(priceDto.getTimestamp())
                    .build();
                
                MarketPrice saved = marketPriceRepository.save(marketPrice);
                return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
            })
            .orElse(ResponseEntity.badRequest().build());
    }
    
    private MarketPriceDto toDto(MarketPrice marketPrice) {
        return MarketPriceDto.builder()
            .id(marketPrice.getId())
            .ticker(marketPrice.getSymbol().getTicker())
            .price(marketPrice.getPrice())
            .volume(marketPrice.getVolume())
            .timestamp(marketPrice.getTimestamp())
            .build();
    }
}

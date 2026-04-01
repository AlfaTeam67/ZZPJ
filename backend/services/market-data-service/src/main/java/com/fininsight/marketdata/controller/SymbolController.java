package com.fininsight.marketdata.controller;

import com.fininsight.marketdata.entity.SupportedSymbol;
import com.fininsight.marketdata.dto.SymbolDto;
import com.fininsight.marketdata.repository.SupportedSymbolRepository;
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
@RequestMapping("/api/symbols")
@RequiredArgsConstructor
@Tag(name = "Symbols", description = "Symbol management API")
@SecurityRequirement(name = "bearerAuth")
public class SymbolController {
    
    private final SupportedSymbolRepository symbolRepository;
    
    @GetMapping
    @Operation(summary = "Get all symbols")
    public ResponseEntity<List<SymbolDto>> getAllSymbols() {
        List<SymbolDto> symbols = symbolRepository.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(symbols);
    }
    
    @GetMapping("/{symbol}")
    @Operation(summary = "Get symbol by symbol code")
    public ResponseEntity<SymbolDto> getSymbolById(@PathVariable String symbol) {
        return symbolRepository.findById(symbol)
            .map(foundSymbol -> ResponseEntity.ok(toDto(foundSymbol)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Create new symbol")
    public ResponseEntity<SymbolDto> createSymbol(@Valid @RequestBody SymbolDto symbolDto) {
        SupportedSymbol symbol = SupportedSymbol.builder()
            .symbol(symbolDto.getSymbol())
            .type(symbolDto.getType())
            .apiSource(symbolDto.getApiSource())
            .active(symbolDto.isActive())
            .baseCurrency(symbolDto.getBaseCurrency())
            .build();
        
        SupportedSymbol saved = symbolRepository.save(symbol);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }
    
    private SymbolDto toDto(SupportedSymbol symbol) {
        return SymbolDto.builder()
            .symbol(symbol.getSymbol())
            .type(symbol.getType())
            .apiSource(symbol.getApiSource())
            .active(symbol.isActive())
            .baseCurrency(symbol.getBaseCurrency())
            .addedAt(symbol.getAddedAt())
            .build();
    }
}

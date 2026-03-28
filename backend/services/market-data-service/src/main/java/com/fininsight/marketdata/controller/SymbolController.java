package com.fininsight.marketdata.controller;

import com.fininsight.marketdata.dto.SymbolDto;
import com.fininsight.marketdata.entity.Symbol;
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
@RequestMapping("/api/symbols")
@RequiredArgsConstructor
@Tag(name = "Symbols", description = "Symbol management API")
@SecurityRequirement(name = "bearerAuth")
public class SymbolController {
    
    private final SymbolRepository symbolRepository;
    
    @GetMapping
    @Operation(summary = "Get all symbols")
    public ResponseEntity<List<SymbolDto>> getAllSymbols() {
        List<SymbolDto> symbols = symbolRepository.findAll().stream()
            .map(this::toDto)
            .collect(Collectors.toList());
        return ResponseEntity.ok(symbols);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get symbol by ID")
    public ResponseEntity<SymbolDto> getSymbolById(@PathVariable Long id) {
        return symbolRepository.findById(id)
            .map(symbol -> ResponseEntity.ok(toDto(symbol)))
            .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    @Operation(summary = "Create new symbol")
    public ResponseEntity<SymbolDto> createSymbol(@Valid @RequestBody SymbolDto symbolDto) {
        Symbol symbol = Symbol.builder()
            .ticker(symbolDto.getTicker())
            .name(symbolDto.getName())
            .type(symbolDto.getType())
            .build();
        
        Symbol saved = symbolRepository.save(symbol);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }
    
    private SymbolDto toDto(Symbol symbol) {
        return SymbolDto.builder()
            .id(symbol.getId())
            .ticker(symbol.getTicker())
            .name(symbol.getName())
            .type(symbol.getType())
            .createdAt(symbol.getCreatedAt())
            .build();
    }
}

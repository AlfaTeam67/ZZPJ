package com.fininsight.marketdata.service;

import com.fininsight.marketdata.cache.PriceCacheService;
import com.fininsight.marketdata.dto.symbol.CreateSymbolRequest;
import com.fininsight.marketdata.dto.symbol.SymbolResponse;
import com.fininsight.marketdata.dto.symbol.UpdateSymbolRequest;
import com.fininsight.marketdata.entity.SupportedSymbol;
import com.fininsight.marketdata.exception.SymbolAlreadyExistsException;
import com.fininsight.marketdata.exception.SymbolNotFoundException;
import com.fininsight.marketdata.mapper.SymbolMapper;
import com.fininsight.marketdata.repository.SupportedSymbolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SymbolService {

    private final SupportedSymbolRepository symbolRepository;
    private final PriceCacheService cacheService;
    private final SymbolMapper symbolMapper;

    @Transactional(readOnly = true)
    public List<SymbolResponse> getAllSymbols() {
        return symbolMapper.toResponseList(symbolRepository.findAll());
    }

    @Transactional(readOnly = true)
    public SymbolResponse getSymbol(String symbol) {
        return symbolMapper.toResponse(findOrThrow(symbol));
    }

    @Transactional
    public SymbolResponse createSymbol(CreateSymbolRequest request) {
        String ticker = request.getSymbol().toUpperCase();
        if (symbolRepository.existsById(ticker)) {
            throw new SymbolAlreadyExistsException(ticker);
        }

        SupportedSymbol entity = symbolMapper.toEntity(request);
        // normalise to upper-case in case the caller sent lower-case
        entity = SupportedSymbol.builder()
                .symbol(ticker)
                .type(entity.getType())
                .apiSource(entity.getApiSource())
                .active(entity.isActive())
                .baseCurrency(entity.getBaseCurrency())
                .build();

        SupportedSymbol saved = symbolRepository.save(entity);

        // new symbol → invalidate the symbols-list cache
        cacheService.evictAllSymbols();
        log.info("Created symbol: {}", ticker);

        return symbolMapper.toResponse(saved);
    }

    @Transactional
    public SymbolResponse updateSymbol(String symbol, UpdateSymbolRequest request) {
        SupportedSymbol entity = findOrThrow(symbol);

        symbolMapper.updateEntity(request, entity);
        SupportedSymbol saved = symbolRepository.save(entity);

        // symbol data changed → evict both the price cache entry and the list cache
        cacheService.evictPrice(symbol);
        cacheService.evictAllSymbols();
        log.info("Updated symbol: {}", symbol);

        return symbolMapper.toResponse(saved);
    }

    @Transactional
    public void deleteSymbol(String symbol) {
        SupportedSymbol entity = findOrThrow(symbol);
        symbolRepository.delete(entity);

        // evict all related cache entries
        cacheService.evictPrice(symbol);
        cacheService.evictAllSymbols();
        log.info("Deleted symbol: {}", symbol);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private SupportedSymbol findOrThrow(String symbol) {
        return symbolRepository.findById(symbol)
                .orElseThrow(() -> new SymbolNotFoundException(symbol));
    }
}

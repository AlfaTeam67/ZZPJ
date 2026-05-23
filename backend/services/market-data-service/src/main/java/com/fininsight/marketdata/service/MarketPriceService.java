package com.fininsight.marketdata.service;

import com.fininsight.marketdata.cache.PriceCacheEntry;
import com.fininsight.marketdata.cache.PriceCacheService;
import com.fininsight.marketdata.dto.price.CreateMarketPriceRequest;
import com.fininsight.marketdata.dto.price.LatestPriceResponse;
import com.fininsight.marketdata.dto.price.MarketPriceResponse;
import com.fininsight.marketdata.entity.PriceSnapshot;
import com.fininsight.marketdata.entity.SupportedSymbol;
import com.fininsight.marketdata.exception.PriceSnapshotNotFoundException;
import com.fininsight.marketdata.exception.SymbolNotFoundException;
import com.fininsight.marketdata.mapper.MarketPriceMapper;
import com.fininsight.marketdata.repository.PriceSnapshotRepository;
import com.fininsight.marketdata.repository.SupportedSymbolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MarketPriceService {

    private final PriceSnapshotRepository snapshotRepository;
    private final SupportedSymbolRepository symbolRepository;
    private final PriceCacheService cacheService;
    private final MarketPriceMapper priceMapper;

    // -------------------------------------------------------------------------
    // Queries
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<MarketPriceResponse> getLatestPrices() {
        return priceMapper.toResponseList(snapshotRepository.findLatestSnapshotsForActiveSymbols());
    }

    /**
     * Returns the latest price for a symbol.
     * <p>
     * Contract consumed by portfolio-manager's {@code MarketDataClient}:
     * always returns {@link LatestPriceResponse} with at least {@code symbol},
     * {@code price} and {@code currency} populated.
     * Cache-hit is indicated via the {@code cached} flag.
     */
    @Transactional(readOnly = true)
    public LatestPriceResponse getLatestPriceBySymbol(String ticker) {
        // 1. Try Redis cache first
        Optional<PriceCacheEntry> cached = cacheService.getPrice(ticker);
        if (cached.isPresent()) {
            PriceCacheEntry entry = cached.get();
            log.debug("Cache hit for symbol: {}", ticker);
            return LatestPriceResponse.builder()
                    .symbol(entry.symbol())
                    .price(entry.price())
                    .currency(entry.currency())
                    .changePct24h(entry.changePct24h())
                    .fetchedAt(entry.fetchedAt())
                    .cached(true)
                    .build();
        }

        // 2. Fall back to DB
        SupportedSymbol symbol = symbolRepository.findBySymbolAndActiveTrue(ticker)
                .orElseThrow(() -> new SymbolNotFoundException(ticker));

        PriceSnapshot snapshot = snapshotRepository
                .findTopBySymbolOrderByFetchedAtDesc(symbol)
                .orElseThrow(() -> new SymbolNotFoundException(
                        "No price data available for symbol: " + ticker));

        LatestPriceResponse response = priceMapper.toLatestResponse(snapshot);

        // Populate cache for subsequent calls
        cacheService.cachePrice(new PriceCacheEntry(
                snapshot.getSymbol().getSymbol(),
                snapshot.getPrice(),
                snapshot.getCurrency(),
                snapshot.getChangePct24h(),
                snapshot.getFetchedAt()
        ));

        return response;
    }

    @Transactional(readOnly = true)
    public List<MarketPriceResponse> getPriceHistoryBySymbol(String ticker) {
        SupportedSymbol symbol = symbolRepository.findBySymbolAndActiveTrue(ticker)
                .orElseThrow(() -> new SymbolNotFoundException(ticker));

        return priceMapper.toResponseList(
                snapshotRepository.findBySymbolOrderByFetchedAtDesc(symbol));
    }

    // -------------------------------------------------------------------------
    // Commands
    // -------------------------------------------------------------------------

    @Transactional
    public MarketPriceResponse addPrice(CreateMarketPriceRequest request) {
        SupportedSymbol symbol = symbolRepository.findBySymbolAndActiveTrue(request.getSymbol())
                .orElseThrow(() -> new SymbolNotFoundException(request.getSymbol()));

        PriceSnapshot entity = priceMapper.toEntity(request, symbol);
        if (entity.getFetchedAt() == null) {
            entity.setFetchedAt(Instant.now());
        }

        PriceSnapshot saved = snapshotRepository.save(entity);

        // A new snapshot is the latest price → update cache immediately
        cacheService.cachePrice(new PriceCacheEntry(
                symbol.getSymbol(),
                saved.getPrice(),
                saved.getCurrency(),
                saved.getChangePct24h(),
                saved.getFetchedAt()
        ));

        log.info("Added price snapshot for symbol: {}, price: {}", symbol.getSymbol(), saved.getPrice());
        return priceMapper.toResponse(saved);
    }

    @Transactional
    public void deleteSnapshot(UUID id) {
        PriceSnapshot snapshot = snapshotRepository.findById(id)
                .orElseThrow(() -> new PriceSnapshotNotFoundException(id));

        String ticker = snapshot.getSymbol().getSymbol();
        snapshotRepository.delete(snapshot);

        // Removing a snapshot may affect the latest-price result → evict cache
        cacheService.evictPrice(ticker);
        log.info("Deleted price snapshot: {} for symbol: {}", id, ticker);
    }
}

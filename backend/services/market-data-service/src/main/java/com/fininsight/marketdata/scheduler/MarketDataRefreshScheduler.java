package com.fininsight.marketdata.scheduler;

import com.fininsight.marketdata.cache.PriceCacheEntry;
import com.fininsight.marketdata.cache.PriceCacheService;
import com.fininsight.marketdata.config.MarketDataSchedulerProperties;
import com.fininsight.marketdata.entity.PriceSnapshot;
import com.fininsight.marketdata.entity.SupportedSymbol;
import com.fininsight.marketdata.entity.enums.SymbolType;
import com.fininsight.marketdata.exception.MarketDataUnavailableException;
import com.fininsight.marketdata.exception.SymbolNotFoundException;
import com.fininsight.marketdata.provider.FinnhubQuoteResponse;
import com.fininsight.marketdata.provider.FinnhubSymbolNotFoundException;
import com.fininsight.marketdata.provider.MarketDataProvider;
import com.fininsight.marketdata.repository.PriceSnapshotRepository;
import com.fininsight.marketdata.repository.SupportedSymbolRepository;
import com.fininsight.marketdata.service.MarketPriceService;
import com.fininsight.marketdata.sse.PriceSseBroadcaster;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "market-data.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class MarketDataRefreshScheduler {

    private final SupportedSymbolRepository symbolRepository;
    private final PriceSnapshotRepository snapshotRepository;
    private final PriceCacheService cacheService;
    private final MarketDataProvider marketDataProvider;
    private final MarketDataSchedulerProperties schedulerProperties;
    private final PriceSseBroadcaster broadcaster;
    private final MarketPriceService priceService;

    // -------------------------------------------------------------------------
    // Scheduled triggers
    // -------------------------------------------------------------------------

    @Scheduled(cron = "${market-data.scheduler.cron:0 */5 9-23 * * MON-FRI}", zone = "UTC")
    public void refreshStocks() {
        log.info("Market data refresh triggered (stocks/forex)");
        refreshByTypes(Set.of(SymbolType.STOCK, SymbolType.FOREX));
    }

    @Scheduled(cron = "${market-data.scheduler.cron-crypto:0 */5 * * * *}", zone = "UTC")
    public void refreshCrypto() {
        log.info("Market data refresh triggered (crypto)");
        refreshByTypes(Set.of(SymbolType.CRYPTO));
    }

    @Scheduled(cron = "0 0 23 * * MON-FRI", zone = "UTC")
    public void eodRefresh() {
        log.info("EOD market data refresh triggered");
        refreshByTypes(Set.of(SymbolType.STOCK));
    }

    // -------------------------------------------------------------------------
    // Core refresh logic
    // -------------------------------------------------------------------------

    public void refreshByTypes(Set<SymbolType> types) {
        if (!schedulerProperties.isEnabled()) {
            log.debug("Market data scheduler is disabled — skipping refresh");
            return;
        }

        List<SupportedSymbol> symbols = symbolRepository.findByActiveTrueAndTypeIn(types);
        if (symbols.isEmpty()) {
            log.debug("No active symbols found for types: {} — nothing to refresh", types);
            return;
        }

        log.info("Starting market data refresh for {} symbol(s) of types: {}", symbols.size(), types);
        int successCount = 0;
        int failureCount = 0;

        for (SupportedSymbol symbol : symbols) {
            try {
                fetchAndPersist(symbol);
                successCount++;
                throttle(symbol.getSymbol());
            } catch (MarketDataUnavailableException e) {
                log.warn("Market data provider unavailable for symbol: {} — skipping", symbol.getSymbol());
                failureCount++;
            } catch (FinnhubSymbolNotFoundException e) {
                log.warn("No market data available for symbol: {} — skipping", symbol.getSymbol());
                failureCount++;
            } catch (Exception e) {
                log.error("Failed to refresh market data for symbol: {} — skipping. Reason: {}",
                        symbol.getSymbol(), e.getMessage());
                failureCount++;
            }
        }

        log.info("Market data refresh completed — success: {}, skipped/failed: {}", successCount, failureCount);
        broadcaster.broadcast(priceService.getLatestPrices());
    }

    // -------------------------------------------------------------------------
    // Admin trigger (Phase 3)
    // -------------------------------------------------------------------------

    public void triggerImmediateFetch(String symbol) {
        SupportedSymbol sym = symbolRepository.findBySymbolAndActiveTrue(symbol)
                .orElseThrow(() -> new SymbolNotFoundException(symbol));
        fetchAndPersist(sym);
        broadcaster.broadcast(priceService.getLatestPrices());
    }

    // -------------------------------------------------------------------------
    // Per-symbol fetch + persist
    // -------------------------------------------------------------------------

    @Transactional
    protected void fetchAndPersist(SupportedSymbol symbol) {
        FinnhubQuoteResponse quote = marketDataProvider.fetchQuote(symbol.getSymbol());

        BigDecimal price = quote.getCurrentPrice();
        BigDecimal changePct = quote.getPercentChange();
        String currency = resolveCurrency(symbol);
        Instant fetchedAt = resolveTimestamp(quote);

        PriceSnapshot snapshot = PriceSnapshot.builder()
                .symbol(symbol)
                .source(marketDataProvider.providerName())
                .price(price)
                .currency(currency)
                .changePct24h(changePct)
                .fetchedAt(fetchedAt)
                .build();

        snapshotRepository.save(snapshot);

        cacheService.cachePrice(new PriceCacheEntry(
                symbol.getSymbol(),
                price,
                currency,
                changePct,
                fetchedAt
        ));

        log.info("Persisted market price for symbol: {}, price: {}, source: {}",
                symbol.getSymbol(), price, marketDataProvider.providerName());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String resolveCurrency(SupportedSymbol symbol) {
        String base = symbol.getBaseCurrency();
        return (base != null && !base.isBlank()) ? base : "USD";
    }

    private Instant resolveTimestamp(FinnhubQuoteResponse quote) {
        return (quote.getTimestamp() != null && quote.getTimestamp() > 0)
                ? Instant.ofEpochSecond(quote.getTimestamp())
                : Instant.now();
    }

    private void throttle(String symbol) {
        long delayMs = schedulerProperties.getInterRequestDelayMs();
        if (delayMs <= 0) return;
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("Rate-limit throttle interrupted after processing symbol: {}", symbol);
        }
    }
}

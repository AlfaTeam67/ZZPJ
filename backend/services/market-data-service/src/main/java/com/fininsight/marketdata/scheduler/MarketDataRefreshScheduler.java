package com.fininsight.marketdata.scheduler;

import com.fininsight.marketdata.cache.PriceCacheEntry;
import com.fininsight.marketdata.cache.PriceCacheService;
import com.fininsight.marketdata.config.MarketDataSchedulerProperties;
import com.fininsight.marketdata.entity.PriceSnapshot;
import com.fininsight.marketdata.entity.SupportedSymbol;
import com.fininsight.marketdata.provider.FinnhubQuoteResponse;
import com.fininsight.marketdata.provider.FinnhubSymbolNotFoundException;
import com.fininsight.marketdata.provider.MarketDataProvider;
import com.fininsight.marketdata.repository.PriceSnapshotRepository;
import com.fininsight.marketdata.repository.SupportedSymbolRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Periodically fetches real market prices for all active symbols and persists
 * the results as {@link PriceSnapshot} rows, updating the Redis cache atomically.
 *
 * <h3>Design decisions</h3>
 * <ul>
 *   <li>Only active symbols ({@code active = true}) are polled.</li>
 *   <li>Each symbol is fetched sequentially with a configurable inter-request
 *       delay to stay within Finnhub's free-tier rate limit (60 req/min).</li>
 *   <li>Resilience4j {@code @Retry} is applied inside {@link MarketDataProvider#fetchQuote}
 *       so per-symbol failures are retried transparently before reaching the
 *       catch block here.</li>
 *   <li>A failure for one symbol is fully isolated — processing continues for the
 *       remaining symbols regardless.</li>
 *   <li>The scheduler is disabled when {@code market-data.scheduler.enabled=false},
 *       making it trivial to turn off in CI/test environments.</li>
 * </ul>
 */
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

    // -------------------------------------------------------------------------
    // Scheduled triggers
    // -------------------------------------------------------------------------

    /**
     * Trading-hours refresh (Mon-Fri, 09:00-23:00 UTC).
     * Cron is externalised to {@code market-data.scheduler.cron} in Config Server.
     */
    @Scheduled(cron = "${market-data.scheduler.cron:0 */5 9-23 * * MON-FRI}",
               zone = "UTC")
    public void refreshTradingHours() {
        log.info("Market data refresh triggered (trading-hours schedule)");
        refreshAllActiveSymbols();
    }

    /**
     * Always-on refresh for crypto / FX symbols (24/7, less frequent).
     * Cron is externalised to {@code market-data.scheduler.cron-always}.
     */
    @Scheduled(cron = "${market-data.scheduler.cron-always:0 */10 * * * *}",
               zone = "UTC")
    public void refreshAlwaysOn() {
        log.info("Market data refresh triggered (always-on schedule)");
        refreshAllActiveSymbols();
    }

    // -------------------------------------------------------------------------
    // Core refresh logic
    // -------------------------------------------------------------------------

    /**
     * Iterates all active symbols and fetches a fresh quote for each.
     * Failures for individual symbols are caught, logged, and skipped.
     */
    void refreshAllActiveSymbols() {
        if (!schedulerProperties.isEnabled()) {
            log.debug("Market data scheduler is disabled — skipping refresh");
            return;
        }

        List<SupportedSymbol> activeSymbols = symbolRepository.findByActiveTrue();
        if (activeSymbols.isEmpty()) {
            log.debug("No active symbols found — nothing to refresh");
            return;
        }

        log.info("Starting market data refresh for {} active symbol(s)", activeSymbols.size());
        int successCount = 0;
        int failureCount = 0;

        for (SupportedSymbol symbol : activeSymbols) {
            try {
                fetchAndPersist(symbol);
                successCount++;
                throttle(symbol.getSymbol());
            } catch (FinnhubSymbolNotFoundException e) {
                // Provider returned no data — not retried, log at WARN and continue
                log.warn("No market data available for symbol: {} — skipping", symbol.getSymbol());
                failureCount++;
            } catch (Exception e) {
                // Retry exhausted or unexpected error — log at ERROR, never propagate
                log.error("Failed to refresh market data for symbol: {} after retries — skipping. Reason: {}",
                        symbol.getSymbol(), e.getMessage());
                failureCount++;
            }
        }

        log.info("Market data refresh completed — success: {}, skipped/failed: {}",
                successCount, failureCount);
    }

    // -------------------------------------------------------------------------
    // Per-symbol fetch + persist
    // -------------------------------------------------------------------------

    /**
     * Fetches a quote for one symbol and persists it as a new {@link PriceSnapshot},
     * then updates the Redis cache so read-paths see the fresh price immediately.
     *
     * <p>Wrapped in its own transaction so a DB failure for one symbol does not
     * roll back already-committed snapshots from previous iterations.</p>
     */
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

        // Immediately push to cache so in-flight reads get the latest price
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

    /**
     * Derives the currency for a snapshot.
     * Uses the symbol's {@code baseCurrency} when set; defaults to {@code "USD"}.
     */
    private String resolveCurrency(SupportedSymbol symbol) {
        String base = symbol.getBaseCurrency();
        return (base != null && !base.isBlank()) ? base : "USD";
    }

    /**
     * Converts Finnhub's Unix-epoch timestamp to {@link Instant}.
     * Falls back to {@link Instant#now()} when the timestamp is absent.
     */
    private Instant resolveTimestamp(FinnhubQuoteResponse quote) {
        return (quote.getTimestamp() != null && quote.getTimestamp() > 0)
                ? Instant.ofEpochSecond(quote.getTimestamp())
                : Instant.now();
    }

    /**
     * Introduces a short pause between consecutive Finnhub requests to avoid
     * breaching the free-tier rate limit (60 req/min ≈ 1 req/s).
     *
     * <p>The delay is configurable via {@code market-data.scheduler.inter-request-delay-ms}.</p>
     */
    private void throttle(String symbol) {
        long delayMs = schedulerProperties.getInterRequestDelayMs();
        if (delayMs <= 0) {
            return;
        }
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            log.warn("Rate-limit throttle interrupted after processing symbol: {}", symbol);
        }
    }
}

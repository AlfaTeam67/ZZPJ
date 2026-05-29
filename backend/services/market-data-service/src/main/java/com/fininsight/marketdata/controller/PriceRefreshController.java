package com.fininsight.marketdata.controller;

import com.fininsight.marketdata.entity.enums.SymbolType;
import com.fininsight.marketdata.scheduler.MarketDataRefreshScheduler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/prices/refresh")
@RequiredArgsConstructor
@ConditionalOnBean(MarketDataRefreshScheduler.class)
@Tag(name = "Price Refresh", description = "Admin endpoints for triggering market data refresh")
@SecurityRequirement(name = "bearerAuth")
public class PriceRefreshController {

    private final MarketDataRefreshScheduler scheduler;

    @PostMapping("/{symbol}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Trigger immediate refresh for a symbol",
               description = "Forces a fresh Finnhub fetch for the given symbol and persists the snapshot.")
    public ResponseEntity<Void> refreshSymbol(@PathVariable String symbol) {
        scheduler.triggerImmediateFetch(symbol);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Trigger refresh for all active symbols",
               description = "Enqueues a full refresh of all STOCK, FOREX and CRYPTO symbols. Returns 202 immediately.")
    public ResponseEntity<Void> refreshAll() {
        CompletableFuture.runAsync(() -> {
            scheduler.refreshByTypes(Set.of(SymbolType.STOCK, SymbolType.FOREX));
            scheduler.refreshByTypes(Set.of(SymbolType.CRYPTO));
        });
        return ResponseEntity.accepted().build();
    }
}

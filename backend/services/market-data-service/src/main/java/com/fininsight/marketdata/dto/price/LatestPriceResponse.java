package com.fininsight.marketdata.dto.price;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Stable contract consumed by portfolio-manager's MarketDataClient.
 * Contains only the fields needed for valuation: symbol, price, currency.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Latest known price for a symbol — stable contract for portfolio-manager")
public class LatestPriceResponse {

    @Schema(description = "Ticker symbol", example = "AAPL")
    private String symbol;

    @Schema(description = "Latest price", example = "182.50")
    private BigDecimal price;

    @Schema(description = "Currency", example = "USD")
    private String currency;

    @Schema(description = "24-hour percentage change", example = "1.25")
    private BigDecimal changePct24h;

    @Schema(description = "Timestamp of this price snapshot")
    private Instant fetchedAt;

    @Schema(description = "True when price was served from Redis cache", example = "false")
    private boolean cached;
}

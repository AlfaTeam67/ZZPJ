package com.fininsight.marketdata.dto.price;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response body representing a single price snapshot")
public class MarketPriceResponse {

    @Schema(description = "Snapshot UUID")
    private UUID id;

    @Schema(description = "Ticker symbol", example = "AAPL")
    private String symbol;

    @Schema(description = "Data provider", example = "alphavantage")
    private String source;

    @Schema(description = "Price value", example = "182.50")
    private BigDecimal price;

    @Schema(description = "Currency", example = "USD")
    private String currency;

    @Schema(description = "24-hour percentage change", example = "1.25")
    private BigDecimal changePct24h;

    @Schema(description = "24-hour trading volume", example = "75000000.00")
    private BigDecimal volume24h;

    @Schema(description = "Timestamp when the snapshot was fetched")
    private Instant fetchedAt;
}

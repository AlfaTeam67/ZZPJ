package com.fininsight.marketdata.dto.price;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for recording a new price snapshot")
public class CreateMarketPriceRequest {

    @NotBlank(message = "Symbol is required")
    @Schema(description = "Ticker symbol this snapshot belongs to", example = "AAPL")
    private String symbol;

    @NotBlank(message = "Source is required")
    @Schema(description = "Data provider that supplied this snapshot", example = "alphavantage")
    private String source;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    @Schema(description = "Price value", example = "182.50")
    private BigDecimal price;

    @NotBlank(message = "Currency is required")
    @Schema(description = "Currency denomination", example = "USD")
    private String currency;

    @Schema(description = "24-hour percentage change", example = "1.25")
    private BigDecimal changePct24h;

    @DecimalMin(value = "0.0", message = "Volume must be non-negative")
    @Schema(description = "24-hour trading volume", example = "75000000.00")
    private BigDecimal volume24h;

    @Schema(description = "Timestamp when data was fetched; defaults to now if omitted")
    private Instant fetchedAt;
}

package com.fininsight.marketdata.dto.symbol;

import com.fininsight.marketdata.entity.enums.SymbolType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response body representing a supported symbol")
public class SymbolResponse {

    @Schema(description = "Unique ticker symbol", example = "AAPL")
    private String symbol;

    @Schema(description = "Asset type", example = "STOCK")
    private SymbolType type;

    @Schema(description = "External data provider identifier", example = "alphavantage")
    private String apiSource;

    @Schema(description = "Whether the symbol is actively tracked", example = "true")
    private boolean active;

    @Schema(description = "Base currency", example = "USD")
    private String baseCurrency;

    @Schema(description = "Timestamp when the symbol was added")
    private Instant addedAt;
}

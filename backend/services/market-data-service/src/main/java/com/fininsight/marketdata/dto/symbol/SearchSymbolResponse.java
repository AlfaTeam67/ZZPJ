package com.fininsight.marketdata.dto.symbol;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchSymbolResponse {

    @Schema(description = "Trading symbol (e.g. AAPL, BTCUSDT)", example = "AAPL")
    private String symbol;

    @Schema(description = "Description or company name", example = "Apple Inc")
    private String description;

    @Schema(description = "Type of symbol (e.g. Common Stock, Crypto)", example = "Common Stock")
    private String type;
}

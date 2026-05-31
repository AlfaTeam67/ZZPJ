package com.fininsight.marketdata.dto.symbol;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveSymbolRequest {

    @NotBlank(message = "Symbol is required")
    @Schema(description = "Trading symbol (e.g. AAPL, BTCUSDT)", example = "AAPL")
    private String symbol;

    @NotBlank(message = "Type is required")
    @Schema(description = "Type of symbol: STOCK or CRYPTO", example = "STOCK")
    private String type;
}

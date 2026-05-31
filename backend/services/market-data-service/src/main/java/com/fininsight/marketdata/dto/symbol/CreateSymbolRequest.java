package com.fininsight.marketdata.dto.symbol;

import com.fininsight.marketdata.entity.enums.SymbolType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for creating a new supported symbol")
public class CreateSymbolRequest {

    @NotBlank(message = "Symbol is required")
    @Size(max = 20, message = "Symbol must not exceed 20 characters")
    @Schema(description = "Unique ticker symbol", example = "AAPL")
    private String symbol;

    @NotNull(message = "Type is required")
    @Schema(description = "Asset type", example = "STOCK")
    private SymbolType type;

    @NotBlank(message = "API source is required")
    @Size(max = 50, message = "API source must not exceed 50 characters")
    @Schema(description = "External data provider identifier", example = "alphavantage")
    private String apiSource;

    @Schema(description = "Whether the symbol is actively tracked", example = "true")
    @Builder.Default
    private boolean active = true;

    @Size(max = 10, message = "Base currency must not exceed 10 characters")
    @Schema(description = "Base currency for the symbol (optional)", example = "USD")
    private String baseCurrency;
}

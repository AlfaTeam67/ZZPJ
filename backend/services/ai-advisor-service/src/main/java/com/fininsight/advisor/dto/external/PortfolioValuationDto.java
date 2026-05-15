package com.fininsight.advisor.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Lokalna projekcja PortfolioValuationResponse z portfolio-manager.
 * Trzymamy ją osobno, żeby nie tworzyć inter-service compile-time coupling.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PortfolioValuationDto(
    UUID portfolioId,
    BigDecimal totalValue,
    List<AssetValuationDto> assets,
    Instant valuedAt
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AssetValuationDto(
        String symbol,
        String type,
        BigDecimal quantity,
        BigDecimal avgBuyPrice,
        BigDecimal currentPrice,
        BigDecimal currentValue,
        BigDecimal gainLoss,
        BigDecimal gainLossPct
    ) {}
}

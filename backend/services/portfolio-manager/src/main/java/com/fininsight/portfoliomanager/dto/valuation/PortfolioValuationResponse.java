package com.fininsight.portfoliomanager.dto.valuation;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PortfolioValuationResponse(
    UUID portfolioId,
    BigDecimal totalValue,
    List<AssetValuationDto> assets,
    Instant valuedAt
) {}

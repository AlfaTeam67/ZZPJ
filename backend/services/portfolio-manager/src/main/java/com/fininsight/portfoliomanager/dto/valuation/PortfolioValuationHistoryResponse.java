package com.fininsight.portfoliomanager.dto.valuation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PortfolioValuationHistoryResponse(
    UUID id,
    UUID portfolioId,
    LocalDate valuationDate,
    BigDecimal totalValue,
    Instant createdAt
) {}

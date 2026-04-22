package com.fininsight.portfoliomanager.dto.transaction;

import com.fininsight.portfoliomanager.domain.enums.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
    UUID id,
    UUID portfolioId,
    UUID assetId,
    TransactionType type,
    BigDecimal quantity,
    BigDecimal price,
    String currency,
    BigDecimal fee,
    Instant executedAt,
    String notes
) {}

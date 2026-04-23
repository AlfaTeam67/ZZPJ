package com.fininsight.portfoliomanager.dto.asset;

import com.fininsight.portfoliomanager.domain.enums.AssetType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AssetResponse(
    UUID id,
    AssetType type,
    String symbol,
    BigDecimal quantity,
    BigDecimal avgBuyPrice,
    String currency,
    Instant addedAt
) {}

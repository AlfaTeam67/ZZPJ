package com.fininsight.portfoliomanager.dto.portfolio;

import com.fininsight.portfoliomanager.dto.asset.AssetResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record PortfolioResponse(
    UUID id,
    UUID userId,
    String name,
    String description,
    List<AssetResponse> assets,
    Map<String, BigDecimal> totals,
    Instant createdAt
) {}

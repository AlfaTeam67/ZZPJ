package com.fininsight.portfoliomanager.dto.portfolio;

import java.time.Instant;
import java.util.UUID;

public record PortfolioSummaryResponse(
    UUID id,
    String name,
    String description,
    Instant createdAt
) {}

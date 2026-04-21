package com.fininsight.portfoliomanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioResponse {
    private UUID id;
    private String name;
    private UUID userId;
    private String description;
    private Map<String, BigDecimal> totals;
    private Instant createdAt;
}

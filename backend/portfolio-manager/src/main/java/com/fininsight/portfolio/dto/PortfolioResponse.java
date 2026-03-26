package com.fininsight.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioResponse {
    private Long id;
    private String name;
    private String userId;
    private BigDecimal totalValue;
    private LocalDateTime createdAt;
}

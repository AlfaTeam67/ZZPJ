package com.fininsight.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioRequest {

    @NotBlank(message = "Portfolio name is required")
    private String name;

    @NotNull(message = "Total value is required")
    private BigDecimal totalValue;
}

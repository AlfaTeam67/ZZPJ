package com.fininsight.marketdata.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketPriceDto {
    
    private UUID id;
    
    @NotBlank(message = "Symbol is required")
    private String symbol;
    
    @NotBlank(message = "Source is required")
    private String source;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal price;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    private BigDecimal changePct24h;
    
    @NotNull(message = "Volume is required")
    @DecimalMin(value = "0.0", message = "Volume must be non-negative")
    private BigDecimal volume24h;
    
    @NotNull(message = "Timestamp is required")
    private Instant fetchedAt;
}

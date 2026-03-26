package com.fininsight.marketdata.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class MarketPriceDto {
    
    private Long id;
    
    @NotBlank(message = "Ticker is required")
    private String ticker;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    private BigDecimal price;
    
    @NotNull(message = "Volume is required")
    @DecimalMin(value = "0.0", message = "Volume must be non-negative")
    private BigDecimal volume;
    
    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;
}

package com.fininsight.marketdata.dto;

import com.fininsight.marketdata.entity.enums.SymbolType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymbolDto {
    
    @NotBlank(message = "Symbol is required")
    @Size(max = 20)
    private String symbol;
    
    @NotNull(message = "Type is required")
    private SymbolType type;
    
    @NotBlank(message = "API source is required")
    @Size(max = 50)
    private String apiSource;
    
    private boolean active;
    
    @Size(max = 10)
    private String baseCurrency;
    
    private Instant addedAt;
}

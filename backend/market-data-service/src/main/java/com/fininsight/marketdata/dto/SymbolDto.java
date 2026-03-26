package com.fininsight.marketdata.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SymbolDto {
    
    private Long id;
    
    @NotBlank(message = "Ticker is required")
    @Size(max = 20)
    private String ticker;
    
    @NotBlank(message = "Name is required")
    @Size(max = 255)
    private String name;
    
    @NotBlank(message = "Type is required")
    @Size(max = 20)
    private String type;
    
    private LocalDateTime createdAt;
}

package com.fininsight.advisor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Portfolio ID is required")
    private String portfolioId;
    
    @NotNull(message = "Risk tolerance is required")
    private RiskTolerance riskTolerance;
    
    public enum RiskTolerance {
        LOW, MODERATE, HIGH, AGGRESSIVE
    }
}

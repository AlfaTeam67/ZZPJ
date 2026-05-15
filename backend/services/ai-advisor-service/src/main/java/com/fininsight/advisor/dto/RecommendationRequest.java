package com.fininsight.advisor.dto;

import com.fininsight.advisor.entity.enums.InvestmentHorizon;
import com.fininsight.advisor.entity.enums.RiskTolerance;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Wejście dla generatora rekomendacji.
 * userId nie jest częścią body - jest pobierane z JWT (sub claim).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {

    @NotNull(message = "Portfolio ID is required")
    private UUID portfolioId;

    @NotNull(message = "Risk tolerance is required")
    private RiskTolerance riskTolerance;

    @NotNull(message = "Investment horizon is required")
    private InvestmentHorizon investmentHorizon;
}

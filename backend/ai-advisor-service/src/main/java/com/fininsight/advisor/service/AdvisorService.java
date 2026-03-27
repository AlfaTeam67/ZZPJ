package com.fininsight.advisor.service;

import com.fininsight.advisor.dto.RecommendationRequest;
import com.fininsight.advisor.dto.RecommendationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class AdvisorService {
    
    public RecommendationResponse generateRecommendations(RecommendationRequest request) {
        log.info("Generating recommendations for userId: {}, portfolioId: {}, riskTolerance: {}", 
                request.getUserId(), request.getPortfolioId(), request.getRiskTolerance());
        
        List<String> recommendations = generateMockRecommendations(request.getRiskTolerance());
        BigDecimal confidence = calculateConfidence(request.getRiskTolerance());
        
        return RecommendationResponse.builder()
                .recommendations(recommendations)
                .confidence(confidence)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    private List<String> generateMockRecommendations(RecommendationRequest.RiskTolerance riskTolerance) {
        List<String> recommendations = new ArrayList<>();
        
        switch (riskTolerance) {
            case LOW:
                recommendations.add("Consider increasing bond allocation to 60%");
                recommendations.add("Focus on dividend-paying blue-chip stocks");
                recommendations.add("Maintain emergency fund equal to 6 months expenses");
                recommendations.add("Consider treasury bonds for stable returns");
                break;
            case MODERATE:
                recommendations.add("Diversify into index funds for balanced growth");
                recommendations.add("Consider 60/40 stock-bond split");
                recommendations.add("Review international market exposure");
                recommendations.add("Rebalance portfolio quarterly");
                break;
            case HIGH:
                recommendations.add("Consider diversifying into tech sector");
                recommendations.add("Explore growth stocks in emerging markets");
                recommendations.add("Review allocation in high-growth industries");
                recommendations.add("Consider real estate investment trusts (REITs)");
                break;
            case AGGRESSIVE:
                recommendations.add("Reduce exposure to crypto assets below 10%");
                recommendations.add("Consider options trading for advanced strategies");
                recommendations.add("Explore venture capital opportunities");
                recommendations.add("Monitor volatility and use stop-loss orders");
                break;
        }
        
        return recommendations;
    }
    
    private BigDecimal calculateConfidence(RecommendationRequest.RiskTolerance riskTolerance) {
        // Keep financial confidence score in BigDecimal to avoid floating point precision loss.
        return switch (riskTolerance) {
            case LOW -> new BigDecimal("0.95");
            case MODERATE -> new BigDecimal("0.90");
            case HIGH -> new BigDecimal("0.85");
            case AGGRESSIVE -> new BigDecimal("0.75");
        };
    }
}

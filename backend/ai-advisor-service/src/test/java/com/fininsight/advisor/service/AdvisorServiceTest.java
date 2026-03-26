package com.fininsight.advisor.service;

import com.fininsight.advisor.dto.RecommendationRequest;
import com.fininsight.advisor.dto.RecommendationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AdvisorServiceTest {

    @InjectMocks
    private AdvisorService advisorService;

    @Test
    void shouldGenerateRecommendationsForLowRiskTolerance() {
        // given
        RecommendationRequest request = RecommendationRequest.builder()
                .userId("user123")
                .portfolioId("portfolio456")
                .riskTolerance(RecommendationRequest.RiskTolerance.LOW)
                .build();

        // when
        RecommendationResponse response = advisorService.generateRecommendations(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRecommendations()).isNotEmpty();
        assertThat(response.getRecommendations()).hasSizeGreaterThan(0);
        assertThat(response.getConfidence()).isEqualTo(0.95);
        assertThat(response.getTimestamp()).isNotNull();
    }

    @Test
    void shouldGenerateRecommendationsForModerateRiskTolerance() {
        // given
        RecommendationRequest request = RecommendationRequest.builder()
                .userId("user123")
                .portfolioId("portfolio456")
                .riskTolerance(RecommendationRequest.RiskTolerance.MODERATE)
                .build();

        // when
        RecommendationResponse response = advisorService.generateRecommendations(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRecommendations()).isNotEmpty();
        assertThat(response.getConfidence()).isEqualTo(0.90);
    }

    @Test
    void shouldGenerateRecommendationsForHighRiskTolerance() {
        // given
        RecommendationRequest request = RecommendationRequest.builder()
                .userId("user123")
                .portfolioId("portfolio456")
                .riskTolerance(RecommendationRequest.RiskTolerance.HIGH)
                .build();

        // when
        RecommendationResponse response = advisorService.generateRecommendations(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRecommendations()).isNotEmpty();
        assertThat(response.getConfidence()).isEqualTo(0.85);
    }

    @Test
    void shouldGenerateRecommendationsForAggressiveRiskTolerance() {
        // given
        RecommendationRequest request = RecommendationRequest.builder()
                .userId("user123")
                .portfolioId("portfolio456")
                .riskTolerance(RecommendationRequest.RiskTolerance.AGGRESSIVE)
                .build();

        // when
        RecommendationResponse response = advisorService.generateRecommendations(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getRecommendations()).isNotEmpty();
        assertThat(response.getConfidence()).isEqualTo(0.75);
    }
}

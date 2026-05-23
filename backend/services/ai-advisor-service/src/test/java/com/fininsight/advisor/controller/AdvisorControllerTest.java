package com.fininsight.advisor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fininsight.advisor.config.TestSecurityConfig;
import com.fininsight.advisor.dto.RecommendationRequest;
import com.fininsight.advisor.dto.RecommendationResponse;
import com.fininsight.advisor.entity.enums.InvestmentHorizon;
import com.fininsight.advisor.entity.enums.RiskTolerance;
import com.fininsight.advisor.service.AdvisorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdvisorController.class)
@Import(TestSecurityConfig.class)
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "eureka.client.enabled=false"
})
class AdvisorControllerTest {

    private static final String USER_SUB = "f47ac10b-58cc-4372-a567-0e02b2c3d479";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdvisorService advisorService;

    @Test
    void shouldReturnRecommendation() throws Exception {
        UUID portfolioId = UUID.randomUUID();
        UUID recommendationId = UUID.randomUUID();
        RecommendationRequest request = RecommendationRequest.builder()
            .portfolioId(portfolioId)
            .riskTolerance(RiskTolerance.MODERATE)
            .investmentHorizon(InvestmentHorizon.MID_TERM)
            .build();

        RecommendationResponse response = RecommendationResponse.builder()
            .id(recommendationId)
            .portfolioId(portfolioId)
            .summary("Portfolio looks balanced.")
            .bulletPoints(List.of("Hold NVDA", "Diversify"))
            .fullText("RISK_SCORE=4.5")
            .newsContext(List.of())
            .riskScore(new BigDecimal("4.50"))
            .modelId("nvidia/nemotron-3-super-120b-a12b:free")
            .createdAt(Instant.now())
            .build();

        when(advisorService.generateRecommendation(eq(UUID.fromString(USER_SUB)), anyString(), any()))
            .thenReturn(response);

        mockMvc.perform(post("/api/recommendations")
                .with(csrf())
                .with(jwt().jwt(builder -> builder.subject(USER_SUB)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(recommendationId.toString()))
            .andExpect(jsonPath("$.bulletPoints").isArray())
            .andExpect(jsonPath("$.riskScore").value(4.5))
            .andExpect(jsonPath("$.modelId").value("nvidia/nemotron-3-super-120b-a12b:free"));
    }

    @Test
    void shouldRejectMissingPortfolioId() throws Exception {
        RecommendationRequest request = RecommendationRequest.builder()
            .riskTolerance(RiskTolerance.MODERATE)
            .investmentHorizon(InvestmentHorizon.MID_TERM)
            .build();

        mockMvc.perform(post("/api/recommendations")
                .with(csrf())
                .with(jwt().jwt(builder -> builder.subject(USER_SUB)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectMissingHorizon() throws Exception {
        RecommendationRequest request = RecommendationRequest.builder()
            .portfolioId(UUID.randomUUID())
            .riskTolerance(RiskTolerance.LOW)
            .build();

        mockMvc.perform(post("/api/recommendations")
                .with(csrf())
                .with(jwt().jwt(builder -> builder.subject(USER_SUB)))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}

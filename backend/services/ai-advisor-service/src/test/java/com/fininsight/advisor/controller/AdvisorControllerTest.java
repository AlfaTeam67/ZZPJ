package com.fininsight.advisor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fininsight.advisor.config.TestSecurityConfig;
import com.fininsight.advisor.dto.RecommendationRequest;
import com.fininsight.advisor.dto.RecommendationResponse;
import com.fininsight.advisor.service.AdvisorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdvisorController.class)
@Import(TestSecurityConfig.class)
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "spring.cloud.discovery.enabled=false",
    "eureka.client.enabled=false"
})
class AdvisorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdvisorService advisorService;

    @Test
    @WithMockUser
    void shouldReturnRecommendations() throws Exception {
        // given
        RecommendationRequest request = RecommendationRequest.builder()
                .userId("user123")
                .portfolioId("portfolio456")
                .riskTolerance(RecommendationRequest.RiskTolerance.MODERATE)
                .build();

        RecommendationResponse response = RecommendationResponse.builder()
                .recommendations(Arrays.asList("Recommendation 1", "Recommendation 2"))
                .confidence(new BigDecimal("0.90"))
                .timestamp(LocalDateTime.now())
                .build();

        when(advisorService.generateRecommendations(any())).thenReturn(response);

        // when & then
        mockMvc.perform(post("/api/recommendations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.confidence").value(0.90));
    }

    @Test
    @WithMockUser
    void shouldReturnBadRequestForInvalidRequest() throws Exception {
        // given - invalid request without required fields
        RecommendationRequest request = RecommendationRequest.builder().build();

        // when & then
        mockMvc.perform(post("/api/recommendations")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

}

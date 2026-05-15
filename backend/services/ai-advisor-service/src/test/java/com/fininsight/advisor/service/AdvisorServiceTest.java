package com.fininsight.advisor.service;

import com.fininsight.advisor.client.PortfolioClient;
import com.fininsight.advisor.client.llm.LlmChatClient;
import com.fininsight.advisor.dto.RecommendationRequest;
import com.fininsight.advisor.dto.RecommendationResponse;
import com.fininsight.advisor.dto.external.PortfolioValuationDto;
import com.fininsight.advisor.entity.LlmProvider;
import com.fininsight.advisor.entity.NewsCache;
import com.fininsight.advisor.entity.Recommendation;
import com.fininsight.advisor.entity.enums.InvestmentHorizon;
import com.fininsight.advisor.entity.enums.NewsProvider;
import com.fininsight.advisor.entity.enums.RiskTolerance;
import com.fininsight.advisor.repository.RecommendationNewsRepository;
import com.fininsight.advisor.repository.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdvisorServiceTest {

    @Mock private PortfolioClient portfolioClient;
    @Mock private NewsAggregatorService newsAggregator;
    @Mock private LlmInvocationService llmInvocationService;
    @Mock private RecommendationRepository recommendationRepository;
    @Mock private RecommendationNewsRepository recommendationNewsRepository;

    private final PromptBuilder promptBuilder = new PromptBuilder();
    private final LlmResponseParser parser = new LlmResponseParser();

    private AdvisorService advisorService;

    @BeforeEach
    void setUp() {
        advisorService = new AdvisorService(
            portfolioClient,
            newsAggregator,
            promptBuilder,
            llmInvocationService,
            parser,
            recommendationRepository,
            recommendationNewsRepository
        );
    }

    @Test
    void shouldGenerateRecommendationGroundedInPortfolioAndNews() {
        UUID userId = UUID.randomUUID();
        UUID portfolioId = UUID.randomUUID();
        String token = "jwt-token";

        var valuation = new PortfolioValuationDto(
            portfolioId,
            new BigDecimal("12500.00"),
            List.of(new PortfolioValuationDto.AssetValuationDto(
                "NVDA", "STOCK",
                new BigDecimal("5"), new BigDecimal("400"),
                new BigDecimal("500"), new BigDecimal("2500"),
                new BigDecimal("500"), new BigDecimal("25.00"))),
            Instant.now()
        );
        when(portfolioClient.getValuation(eq(portfolioId), eq(token))).thenReturn(valuation);

        NewsCache headline = NewsCache.builder()
            .id(UUID.randomUUID())
            .headline("NVIDIA beats earnings expectations")
            .source("Reuters")
            .provider(NewsProvider.FINNHUB)
            .symbol("NVDA")
            .fetchedAt(Instant.now())
            .build();
        when(newsAggregator.getNewsForSymbols(any())).thenReturn(List.of(headline));

        LlmProvider provider = LlmProvider.builder()
            .id((short) 1).name("Test").modelId("nvidia/nemotron-3-super-120b-a12b:free")
            .apiKeyEnv("OPENROUTER_API_KEY").active(true).priority((short) 1).addedAt(Instant.now()).build();
        var llmResult = new LlmInvocationService.LlmInvocationResult(
            provider,
            new LlmChatClient.LlmCompletion(
                "The portfolio is well positioned for upside given strong earnings.\n"
                + "- Hold NVDA position\n"
                + "- Consider partial profit taking\n"
                + "- Diversify into other AI stocks\n"
                + "RISK_SCORE=6.5",
                "nvidia/nemotron-3-super-120b-a12b:free", 100, 50)
        );
        when(llmInvocationService.invoke(anyList())).thenReturn(llmResult);

        when(recommendationRepository.save(any(Recommendation.class))).thenAnswer(inv -> {
            Recommendation r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            r.setCreatedAt(Instant.now());
            return r;
        });

        RecommendationRequest req = RecommendationRequest.builder()
            .portfolioId(portfolioId)
            .riskTolerance(RiskTolerance.MODERATE)
            .investmentHorizon(InvestmentHorizon.MID_TERM)
            .build();

        RecommendationResponse resp = advisorService.generateRecommendation(userId, token, req);

        assertThat(resp).isNotNull();
        assertThat(resp.getId()).isNotNull();
        assertThat(resp.getPortfolioId()).isEqualTo(portfolioId);
        assertThat(resp.getModelId()).isEqualTo("nvidia/nemotron-3-super-120b-a12b:free");
        assertThat(resp.getBulletPoints()).hasSize(3);
        assertThat(resp.getRiskScore()).isEqualByComparingTo(new BigDecimal("6.50"));
        assertThat(resp.getNewsContext()).hasSize(1);
        assertThat(resp.getNewsContext().get(0).getHeadline()).contains("NVIDIA");

        verify(recommendationRepository).save(any(Recommendation.class));
        verify(recommendationNewsRepository).saveAll(anyList());
    }
}

package com.fininsight.advisor.service;

import com.fininsight.advisor.entity.LlmProvider;
import com.fininsight.advisor.entity.NewsCache;
import com.fininsight.advisor.entity.Recommendation;
import com.fininsight.advisor.entity.RecommendationNews;
import com.fininsight.advisor.repository.RecommendationNewsRepository;
import com.fininsight.advisor.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Wyizolowany bean persystencji - ma sens, bo @Transactional na metodzie
 * wewnątrz {@link AdvisorService} nie zadziałałoby (self-invocation omija proxy).
 *
 * REQUIRES_NEW gwarantuje, że krótka transakcja zapisu nie wpadnie do żadnej
 * obejmującej transakcji wywołującego (np. test integracyjny).
 */
@Component
@RequiredArgsConstructor
public class RecommendationPersister {

    private final RecommendationRepository recommendationRepository;
    private final RecommendationNewsRepository recommendationNewsRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Recommendation save(
        UUID userId,
        UUID portfolioId,
        LlmProvider provider,
        String promptSummary,
        String llmResponse,
        BigDecimal riskScore,
        List<NewsCache> news
    ) {
        Recommendation rec = Recommendation.builder()
            .userId(userId)
            .portfolioId(portfolioId)
            .llmProvider(provider)
            .promptSummary(promptSummary)
            .llmResponse(llmResponse)
            .riskScore(riskScore)
            .build();
        Recommendation saved = recommendationRepository.save(rec);

        if (news != null && !news.isEmpty()) {
            List<RecommendationNews> links = news.stream()
                .map(n -> new RecommendationNews(saved, n))
                .toList();
            recommendationNewsRepository.saveAll(links);
        }
        return saved;
    }
}

package com.fininsight.advisor.service;

import com.fininsight.advisor.client.PortfolioClient;
import com.fininsight.advisor.client.llm.LlmChatClient;
import com.fininsight.advisor.dto.RecommendationRequest;
import com.fininsight.advisor.dto.RecommendationResponse;
import com.fininsight.advisor.dto.external.PortfolioValuationDto;
import com.fininsight.advisor.entity.NewsCache;
import com.fininsight.advisor.entity.Recommendation;
import com.fininsight.advisor.entity.RecommendationNews;
import com.fininsight.advisor.entity.enums.RiskTolerance;
import com.fininsight.advisor.exception.RecommendationNotFoundException;
import com.fininsight.advisor.repository.RecommendationNewsRepository;
import com.fininsight.advisor.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvisorService {

    private final PortfolioClient portfolioClient;
    private final NewsAggregatorService newsAggregator;
    private final PromptBuilder promptBuilder;
    private final LlmInvocationService llmInvocationService;
    private final LlmResponseParser parser;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationNewsRepository recommendationNewsRepository;

    @Transactional
    public RecommendationResponse generateRecommendation(UUID userId, String bearerToken, RecommendationRequest request) {
        log.info("Generating recommendation user={} portfolio={} risk={} horizon={}",
            userId, request.getPortfolioId(), request.getRiskTolerance(), request.getInvestmentHorizon());

        // 1) Pobierz portfel z portfolio-manager (z propagowanym JWT).
        PortfolioValuationDto valuation = portfolioClient.getValuation(request.getPortfolioId(), bearerToken);

        // 2) Pobierz newsy dla unikalnych symboli z portfela.
        Set<String> symbols = extractSymbols(valuation);
        List<NewsCache> news = newsAggregator.getNewsForSymbols(symbols);
        log.info("Aggregated {} news entries for {} symbols (per provider: {})",
            news.size(), symbols.size(), newsAggregator.countByProvider(news));

        // 3) Zbuduj prompt i zawołaj LLM (z fallbackiem między providerami).
        List<LlmChatClient.ChatMessage> messages = promptBuilder.build(
            valuation, news, request.getRiskTolerance(), request.getInvestmentHorizon());
        var invocation = llmInvocationService.invoke(messages);

        // 4) Sparsuj odpowiedź modelu.
        var parsed = parser.parse(invocation.completion().content());
        BigDecimal riskScore = parsed.riskScoreOpt().orElseGet(() -> defaultRiskScore(request.getRiskTolerance()));

        // 5) Zapisz rekomendację + powiązane newsy (M:N).
        Recommendation rec = Recommendation.builder()
            .userId(userId)
            .portfolioId(request.getPortfolioId())
            .llmProvider(invocation.provider())
            .promptSummary(buildPromptSummary(request, valuation, news.size()))
            .llmResponse(parsed.fullText())
            .riskScore(riskScore)
            .build();
        Recommendation saved = recommendationRepository.save(rec);

        if (!news.isEmpty()) {
            List<RecommendationNews> links = news.stream()
                .map(n -> new RecommendationNews(saved, n))
                .toList();
            recommendationNewsRepository.saveAll(links);
        }

        return mapToResponse(saved, invocation.provider().getModelId(), parsed, news);
    }

    @Transactional(readOnly = true)
    public RecommendationResponse getById(UUID id, UUID userId) {
        Recommendation rec = recommendationRepository.findById(id)
            .orElseThrow(() -> new RecommendationNotFoundException("Recommendation " + id + " not found"));
        if (!rec.getUserId().equals(userId)) {
            // Nie wystawiamy faktu istnienia cudzej rekomendacji - 404 jest świadomy.
            throw new RecommendationNotFoundException("Recommendation " + id + " not found");
        }
        var related = recommendationNewsRepository.findByRecommendation_Id(rec.getId());
        var news = related.stream().map(RecommendationNews::getNews).toList();
        var parsed = parser.parse(rec.getLlmResponse());
        return mapToResponse(rec, rec.getLlmProvider() != null ? rec.getLlmProvider().getModelId() : null, parsed, news);
    }

    @Transactional(readOnly = true)
    public Page<RecommendationResponse> listForUser(UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return recommendationRepository.findByUserId(userId, pageable)
            .map(rec -> mapToResponse(rec, rec.getLlmProvider() != null ? rec.getLlmProvider().getModelId() : null,
                parser.parse(rec.getLlmResponse()), List.of()));
    }

    @Transactional(readOnly = true)
    public Page<RecommendationResponse> listForPortfolio(UUID portfolioId, UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return recommendationRepository.findByPortfolioId(portfolioId, pageable)
            .map(rec -> {
                if (!rec.getUserId().equals(userId)) {
                    return null;
                }
                return mapToResponse(rec,
                    rec.getLlmProvider() != null ? rec.getLlmProvider().getModelId() : null,
                    parser.parse(rec.getLlmResponse()), List.of());
            });
    }

    private Set<String> extractSymbols(PortfolioValuationDto valuation) {
        Set<String> symbols = new LinkedHashSet<>();
        if (valuation != null && valuation.assets() != null) {
            for (var a : valuation.assets()) {
                if (a.symbol() != null && !a.symbol().isBlank()) {
                    symbols.add(a.symbol().trim().toUpperCase());
                }
            }
        }
        return symbols;
    }

    private String buildPromptSummary(RecommendationRequest req, PortfolioValuationDto valuation, int newsCount) {
        return "risk=%s horizon=%s assets=%d news=%d total=%s".formatted(
            req.getRiskTolerance(),
            req.getInvestmentHorizon(),
            valuation.assets() == null ? 0 : valuation.assets().size(),
            newsCount,
            valuation.totalValue() == null ? "0" : valuation.totalValue().toPlainString()
        );
    }

    private BigDecimal defaultRiskScore(RiskTolerance risk) {
        return switch (risk) {
            case LOW -> new BigDecimal("2.50");
            case MODERATE -> new BigDecimal("4.50");
            case HIGH -> new BigDecimal("6.50");
            case AGGRESSIVE -> new BigDecimal("8.50");
        };
    }

    private RecommendationResponse mapToResponse(
        Recommendation rec,
        String modelId,
        LlmResponseParser.ParsedRecommendation parsed,
        List<NewsCache> news
    ) {
        return RecommendationResponse.builder()
            .id(rec.getId())
            .portfolioId(rec.getPortfolioId())
            .summary(parsed.summary())
            .bulletPoints(parsed.bullets())
            .fullText(parsed.fullText())
            .newsContext(news.stream().map(this::toNewsItem).toList())
            .riskScore(rec.getRiskScore())
            .modelId(modelId)
            .createdAt(rec.getCreatedAt())
            .build();
    }

    private RecommendationResponse.NewsItem toNewsItem(NewsCache n) {
        return RecommendationResponse.NewsItem.builder()
            .id(n.getId())
            .headline(n.getHeadline())
            .source(n.getSource())
            .provider(n.getProvider() != null ? n.getProvider().name() : null)
            .symbol(n.getSymbol())
            .url(n.getUrl())
            .sentiment(n.getSentiment() != null ? n.getSentiment().name() : null)
            .build();
    }

}

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
import com.fininsight.advisor.exception.PortfolioNotAvailableException;
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
    private final RecommendationPersister recommendationPersister;
    private final RecommendationRepository recommendationRepository;
    private final RecommendationNewsRepository recommendationNewsRepository;

    /**
     * Orkiestracja generowania rekomendacji.
     *
     * UWAGA: metoda celowo NIE jest @Transactional. Wywołania zewnętrzne
     * (portfolio-manager, Finnhub/NewsAPI, LLM) trwają sekundy i blokowałyby
     * połączenie HikariCP (transaction pinning). Zapis do bazy idzie tylko
     * w kontrolowanej transakcji w {@link RecommendationPersister}.
     */
    public RecommendationResponse generateRecommendation(UUID userId, String bearerToken, RecommendationRequest request) {
        log.info("Generating recommendation user={} portfolio={} risk={} horizon={}",
            userId, request.getPortfolioId(), request.getRiskTolerance(), request.getInvestmentHorizon());

        // 1) Pobierz portfel z portfolio-manager (z propagowanym JWT). Bez transakcji.
        PortfolioValuationDto valuation = portfolioClient.getValuation(request.getPortfolioId(), bearerToken);
        if (valuation == null) {
            throw new PortfolioNotAvailableException(
                "portfolio-manager returned empty body for portfolio " + request.getPortfolioId());
        }

        // 2) Pobierz newsy dla unikalnych symboli z portfela. Każdy upsert w osobnej krótkiej transakcji.
        Set<String> symbols = extractSymbols(valuation);
        List<NewsCache> news = newsAggregator.getNewsForSymbols(symbols);
        log.info("Aggregated {} news entries for {} symbols (per provider: {})",
            news.size(), symbols.size(), newsAggregator.countByProvider(news));

        // 3) Zbuduj prompt i zawołaj LLM (z fallbackiem między providerami). Bez transakcji.
        List<LlmChatClient.ChatMessage> messages = promptBuilder.build(
            valuation, news, request.getRiskTolerance(), request.getInvestmentHorizon());
        var invocation = llmInvocationService.invoke(messages);

        // 4) Sparsuj odpowiedź modelu.
        var parsed = parser.parse(invocation.completion().content());
        BigDecimal riskScore = parsed.riskScoreOpt().orElseGet(() -> defaultRiskScore(request.getRiskTolerance()));

        // 5) Persystencja w jednej zwięzłej transakcji - przez osobny bean, żeby AOP zadziałało.
        Recommendation saved = recommendationPersister.save(
            userId,
            request.getPortfolioId(),
            invocation.provider(),
            buildPromptSummary(request, valuation, news.size()),
            parsed.fullText(),
            riskScore,
            news);

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

    /**
     * Filtrujemy po (portfolioId, userId) na poziomie SQL, żeby:
     *  - nie wyciekała informacja o liczbie cudzych rekomendacji,
     *  - w odpowiedzi nie pojawiały się nullowe elementy zwracane z mappera.
     */
    @Transactional(readOnly = true)
    public Page<RecommendationResponse> listForPortfolio(UUID portfolioId, UUID userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return recommendationRepository.findByPortfolioIdAndUserId(portfolioId, userId, pageable)
            .map(rec -> mapToResponse(
                rec,
                rec.getLlmProvider() != null ? rec.getLlmProvider().getModelId() : null,
                parser.parse(rec.getLlmResponse()),
                List.of()));
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

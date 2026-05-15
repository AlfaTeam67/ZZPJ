package com.fininsight.advisor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {

    private UUID id;
    private UUID portfolioId;
    private String summary;
    private String fullText;
    private List<String> bulletPoints;
    private List<NewsItem> newsContext;
    private BigDecimal riskScore;
    private String modelId;
    private Instant createdAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NewsItem {
        private UUID id;
        private String headline;
        private String source;
        private String provider;
        private String symbol;
        private String url;
        private String sentiment;
    }
}

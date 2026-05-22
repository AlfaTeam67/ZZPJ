package com.fininsight.advisor.client.news;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fininsight.advisor.entity.enums.NewsProvider;
import com.fininsight.advisor.entity.enums.NewsSentiment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * NewsAPI.org: GET https://newsapi.org/v2/everything?q=...&from=...&language=en
 * Free tier: 100 req/dobę, dane do 1 miesiąca wstecz, dobre dla ogólnego kontekstu rynkowego.
 */
@Slf4j
@Component
public class NewsApiClient {

    private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

    private final RestClient restClient;
    private final String apiKey;
    private final boolean enabled;

    public NewsApiClient(
        @Qualifier("externalRestClientBuilder") RestClient.Builder builder,
        @Value("${app.newsapi.base-url:https://newsapi.org/v2}") String baseUrl,
        @Value("${app.newsapi.api-key:${NEWSAPI_KEY:}}") String apiKey
    ) {
        this.apiKey = apiKey;
        this.enabled = apiKey != null && !apiKey.isBlank();
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public List<NewsItem> fetchByQuery(String query, String symbol, LocalDate from, int pageSize) {
        if (!enabled) {
            return Collections.emptyList();
        }
        try {
            NewsApiResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                    .path("/everything")
                    .queryParam("q", query)
                    .queryParam("from", from.format(ISO_DATE))
                    .queryParam("language", "en")
                    .queryParam("sortBy", "publishedAt")
                    .queryParam("pageSize", pageSize)
                    .queryParam("apiKey", apiKey)
                    .build())
                .retrieve()
                .body(NewsApiResponse.class);

            if (response == null || response.articles == null) {
                return Collections.emptyList();
            }

            return response.articles.stream()
                .filter(a -> a.title != null && !a.title.isBlank())
                .map(a -> new NewsItem(
                    a.url,
                    a.title,
                    Optional.ofNullable(a.source).map(s -> s.name).orElse("NewsAPI"),
                    a.url,
                    symbol,
                    NewsProvider.NEWSAPI,
                    NewsSentiment.NEUTRAL,
                    parseDate(a.publishedAt)
                ))
                .toList();
        } catch (RestClientResponseException e) {
            log.warn("NewsAPI returned {} for query '{}': {}", e.getStatusCode(), query, e.getMessage());
            return Collections.emptyList();
        } catch (Exception e) {
            log.warn("NewsAPI unreachable for query '{}': {}", query, e.getMessage());
            return Collections.emptyList();
        }
    }

    private Instant parseDate(String iso) {
        try {
            return iso != null ? Instant.parse(iso) : Instant.now();
        } catch (Exception e) {
            return Instant.now();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class NewsApiResponse {
        public String status;
        public Integer totalResults;
        public List<Article> articles;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Article {
        public Source source;
        public String title;
        public String description;
        public String url;
        public String publishedAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Source {
        public String id;
        public String name;
    }
}

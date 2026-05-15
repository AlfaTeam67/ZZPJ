package com.fininsight.advisor.client.news;

import com.fininsight.advisor.entity.enums.NewsProvider;
import com.fininsight.advisor.entity.enums.NewsSentiment;

import java.time.Instant;

/**
 * Wewnętrzna reprezentacja nagłówka. Klienci news-providers mapują tu swoje DTO,
 * dzięki czemu warstwa wyżej operuje na jednym typie.
 */
public record NewsItem(
    String externalId,
    String headline,
    String source,
    String url,
    String symbol,
    NewsProvider provider,
    NewsSentiment sentiment,
    Instant publishedAt
) {}

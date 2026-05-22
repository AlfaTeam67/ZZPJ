package com.fininsight.advisor.service;

import com.fininsight.advisor.client.news.FinnhubNewsClient;
import com.fininsight.advisor.client.news.NewsApiClient;
import com.fininsight.advisor.client.news.NewsItem;
import com.fininsight.advisor.entity.NewsCache;
import com.fininsight.advisor.entity.enums.NewsProvider;
import com.fininsight.advisor.repository.NewsCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Pobiera newsy dla zestawu symboli z Finnhub i NewsAPI, deduplikuje i upsertuje do news_cache.
 *
 * Strategia cache: jeśli w bazie są nieprzeterminowane wpisy dla danego symbolu, używamy ich;
 * w przeciwnym razie wołamy zewnętrzne API. NewsCleanupScheduler i tak posprząta wygasłe.
 *
 * UWAGA: metoda orkiestrująca {@link #getNewsForSymbols} celowo NIE jest @Transactional -
 * trzymanie transakcji przez czas wywołań HTTP do Finnhub/NewsAPI prowadzi do transaction
 * pinningu i wyczerpania puli HikariCP. Każdy upsert leci w osobnej, krótkiej transakcji
 * przez {@link NewsCachePersister}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NewsAggregatorService {

    private final FinnhubNewsClient finnhubClient;
    private final NewsApiClient newsApiClient;
    private final NewsCacheRepository newsCacheRepository;
    private final NewsCachePersister persister;

    @Value("${app.news.lookback-days:7}")
    private int lookbackDays;

    @Value("${app.news.per-symbol-limit:5}")
    private int perSymbolLimit;

    public List<NewsCache> getNewsForSymbols(Collection<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return List.of();
        }

        Instant now = Instant.now();
        LocalDate to = LocalDate.now(ZoneOffset.UTC);
        LocalDate from = to.minusDays(lookbackDays);

        Map<String, NewsCache> deduped = new LinkedHashMap<>();

        for (String symbol : symbols) {
            // 1) cache - krótkie czytanie z bazy
            List<NewsCache> cached = newsCacheRepository.findBySymbolAndExpiresAtAfterOrderByFetchedAtDesc(symbol, now);
            if (!cached.isEmpty()) {
                cached.stream().limit(perSymbolLimit)
                    .forEach(n -> deduped.putIfAbsent(dedupKey(n), n));
                continue;
            }

            // 2) external fetch - poza transakcją
            List<NewsItem> fresh = new ArrayList<>();
            fresh.addAll(finnhubClient.fetchCompanyNews(symbol, from, to, perSymbolLimit));

            if (newsApiClient.isEnabled()) {
                fresh.addAll(newsApiClient.fetchByQuery(symbol, symbol, from, perSymbolLimit));
            }

            // 3) upsert per nagłówek - każdy w osobnej krótkiej transakcji
            for (NewsItem item : fresh) {
                NewsCache saved = persister.upsert(item, lookbackDays);
                deduped.putIfAbsent(dedupKey(saved), saved);
            }
        }

        return new ArrayList<>(deduped.values());
    }

    private String dedupKey(NewsCache n) {
        if (n.getProvider() != null && n.getExternalId() != null) {
            return n.getProvider() + ":" + n.getExternalId();
        }
        return (n.getHeadline() == null ? "" : n.getHeadline().toLowerCase());
    }

    /**
     * Przelicza, ile newsów per provider zostało użytych. Pomocne dla logów / metryk.
     */
    public Map<NewsProvider, Long> countByProvider(List<NewsCache> news) {
        Map<NewsProvider, Long> result = new LinkedHashMap<>();
        for (NewsCache n : news) {
            if (n.getProvider() != null) {
                result.merge(n.getProvider(), 1L, Long::sum);
            }
        }
        return result;
    }

    /**
     * Wyizolowany bean persystencji. Każdy upsert leci w osobnej REQUIRES_NEW transakcji,
     * dzięki czemu Hikari connection nie jest trzymane podczas calli HTTP wcześniej.
     */
    @org.springframework.stereotype.Component
    @lombok.RequiredArgsConstructor
    static class NewsCachePersister {

        private final NewsCacheRepository repository;

        @Transactional(propagation = Propagation.REQUIRES_NEW)
        NewsCache upsert(NewsItem item, int lookbackDays) {
            if (item.externalId() != null) {
                var existing = repository.findByProviderAndExternalId(item.provider(), item.externalId());
                if (existing.isPresent()) {
                    return existing.get();
                }
            }
            Instant now = Instant.now();
            NewsCache entity = NewsCache.builder()
                .headline(truncate(item.headline(), 1000))
                .source(truncate(item.source(), 100))
                .url(item.url())
                .symbol(item.symbol())
                .provider(item.provider())
                .externalId(truncate(item.externalId(), 200))
                .sentiment(item.sentiment())
                .fetchedAt(now)
                .expiresAt(now.plus(Math.max(lookbackDays, 1), ChronoUnit.DAYS))
                .build();
            return repository.save(entity);
        }

        private static String truncate(String s, int max) {
            if (s == null) return null;
            return s.length() <= max ? s : s.substring(0, max);
        }
    }
}

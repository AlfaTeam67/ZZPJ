package com.fininsight.advisor.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "recommendation_news")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"recommendation", "news"})
public class RecommendationNews {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private RecommendationNewsId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("recommendationId")
    @JoinColumn(name = "recommendation_id", nullable = false)
    private Recommendation recommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("newsId")
    @JoinColumn(name = "news_id", nullable = false)
    private NewsCache news;

    public RecommendationNews(Recommendation recommendation, NewsCache news) {
        this.recommendation = recommendation;
        this.news = news;
        this.id = new RecommendationNewsId(recommendation.getId(), news.getId());
    }
}

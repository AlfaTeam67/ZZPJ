CREATE TABLE recommendation_news (
    recommendation_id UUID NOT NULL,
    news_id UUID NOT NULL,
    PRIMARY KEY (recommendation_id, news_id),
    CONSTRAINT fk_recommendation_news_recommendation
        FOREIGN KEY (recommendation_id) REFERENCES recommendations(id) ON DELETE CASCADE,
    CONSTRAINT fk_recommendation_news_news
        FOREIGN KEY (news_id) REFERENCES news_cache(id) ON DELETE CASCADE
);

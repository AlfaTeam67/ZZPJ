package com.fininsight.advisor.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public record RecommendationNewsId(
    UUID recommendationId,
    UUID newsId
) implements Serializable {
}

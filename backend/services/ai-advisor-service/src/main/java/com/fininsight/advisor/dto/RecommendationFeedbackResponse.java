package com.fininsight.advisor.dto;

import java.time.Instant;
import java.util.UUID;

public record RecommendationFeedbackResponse(
    UUID id,
    UUID recommendationId,
    Boolean isPositive,
    String comment,
    Instant createdAt
) {}

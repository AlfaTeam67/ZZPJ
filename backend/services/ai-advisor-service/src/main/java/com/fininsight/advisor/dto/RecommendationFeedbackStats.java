package com.fininsight.advisor.dto;

import java.util.UUID;

public record RecommendationFeedbackStats(
    UUID recommendationId,
    long totalFeedback,
    long positiveFeedback,
    long negativeFeedback,
    double positiveRatio
) {}

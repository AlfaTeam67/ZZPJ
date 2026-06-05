package com.fininsight.advisor.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecommendationFeedbackRequest(
    @NotNull Boolean isPositive,
    @Size(max = 500) String comment
) {}

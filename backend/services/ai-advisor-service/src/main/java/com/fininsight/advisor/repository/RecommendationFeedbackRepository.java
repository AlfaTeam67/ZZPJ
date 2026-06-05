package com.fininsight.advisor.repository;

import com.fininsight.advisor.entity.RecommendationFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecommendationFeedbackRepository extends JpaRepository<RecommendationFeedback, UUID> {

    List<RecommendationFeedback> findByRecommendationId(UUID recommendationId);

    long countByRecommendationIdAndIsPositive(UUID recommendationId, Boolean isPositive);

    long countByRecommendationId(UUID recommendationId);
}

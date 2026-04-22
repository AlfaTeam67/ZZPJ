package com.fininsight.advisor.repository;

import com.fininsight.advisor.entity.RecommendationNews;
import com.fininsight.advisor.entity.RecommendationNewsId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RecommendationNewsRepository extends JpaRepository<RecommendationNews, RecommendationNewsId> {

    List<RecommendationNews> findByRecommendation_Id(UUID recommendationId);
}

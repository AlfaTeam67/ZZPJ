package com.fininsight.advisor.repository;

import com.fininsight.advisor.entity.Recommendation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecommendationRepository extends JpaRepository<Recommendation, UUID> {

    Page<Recommendation> findByUserId(UUID userId, Pageable pageable);

    Page<Recommendation> findByPortfolioIdAndUserId(UUID portfolioId, UUID userId, Pageable pageable);
}

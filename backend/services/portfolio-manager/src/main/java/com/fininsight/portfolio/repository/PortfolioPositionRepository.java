package com.fininsight.portfolio.repository;

import com.fininsight.portfolio.entity.PortfolioPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioPositionRepository extends JpaRepository<PortfolioPosition, Long> {
    List<PortfolioPosition> findByPortfolioId(Long portfolioId);
}

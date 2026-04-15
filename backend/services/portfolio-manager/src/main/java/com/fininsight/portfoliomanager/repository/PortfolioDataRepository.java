package com.fininsight.portfoliomanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.fininsight.portfoliomanager.domain.Portfolio;

import java.util.List;
import java.util.UUID;

public interface PortfolioDataRepository extends JpaRepository<Portfolio, UUID> {
    List<Portfolio> findByUserId(UUID userId);
}

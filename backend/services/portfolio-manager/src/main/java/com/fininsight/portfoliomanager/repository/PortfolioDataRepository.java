package com.fininsight.portfoliomanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import com.fininsight.portfoliomanager.domain.Portfolio;

import java.util.List;
import java.util.UUID;

public interface PortfolioDataRepository extends JpaRepository<Portfolio, UUID> {
    @EntityGraph(attributePaths = "user")
    List<Portfolio> findByUserId(UUID userId);

    @EntityGraph(attributePaths = "user")
    java.util.Optional<Portfolio> findByIdAndUserId(UUID id, UUID userId);
}

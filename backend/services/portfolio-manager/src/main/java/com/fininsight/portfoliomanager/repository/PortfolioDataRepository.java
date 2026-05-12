package com.fininsight.portfoliomanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import com.fininsight.portfoliomanager.domain.Portfolio;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PortfolioDataRepository extends JpaRepository<Portfolio, UUID> {
    @Override
    @EntityGraph(attributePaths = "user")
    Optional<Portfolio> findById(UUID id);

    @EntityGraph(attributePaths = "user")
    List<Portfolio> findByUserId(UUID userId);

    @EntityGraph(attributePaths = "user")
    Optional<Portfolio> findByIdAndUserId(UUID id, UUID userId);
}

package com.fininsight.portfoliomanager.repository;

import com.fininsight.portfoliomanager.domain.Transaction;
import com.fininsight.portfoliomanager.domain.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByPortfolioId(UUID portfolioId);

    Page<Transaction> findByPortfolioId(UUID portfolioId, Pageable pageable);

    List<Transaction> findByAssetId(UUID assetId);

    @Query("""
        SELECT t FROM DataTransaction t
        WHERE t.portfolio.id = :portfolioId
          AND (:from IS NULL OR t.executedAt >= :from)
          AND (:to   IS NULL OR t.executedAt <= :to)
          AND (:type IS NULL OR t.type = :type)
        """)
    Page<Transaction> findByPortfolioIdWithFilters(
        @Param("portfolioId") UUID portfolioId,
        @Param("from") Instant from,
        @Param("to") Instant to,
        @Param("type") TransactionType type,
        Pageable pageable
    );
}

package com.fininsight.portfoliomanager.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.fininsight.portfoliomanager.domain.Asset;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssetRepository extends JpaRepository<Asset, UUID> {
    List<Asset> findByPortfolioId(UUID portfolioId);

    Optional<Asset> findByPortfolioIdAndSymbol(UUID portfolioId, String symbol);

    Optional<Asset> findByPortfolioIdAndId(UUID portfolioId, UUID id);

    @Query("""
        SELECT a.portfolio.id AS portfolioId, COALESCE(SUM(a.quantity * a.avgBuyPrice), 0) AS totalValue
        FROM DataAsset a
        WHERE a.portfolio.id IN :portfolioIds
        GROUP BY a.portfolio.id
        """)
    List<PortfolioTotalValueProjection> findTotalValuesByPortfolioIds(@Param("portfolioIds") Collection<UUID> portfolioIds);

    @Query("""
        SELECT COALESCE(SUM(a.quantity * a.avgBuyPrice), 0)
        FROM DataAsset a
        WHERE a.portfolio.id = :portfolioId
        """)
    BigDecimal calculateTotalValueByPortfolioId(@Param("portfolioId") UUID portfolioId);

    interface PortfolioTotalValueProjection {
        UUID getPortfolioId();

        BigDecimal getTotalValue();
    }
}

package com.fininsight.marketdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.fininsight.marketdata.entity.PriceSnapshot;
import com.fininsight.marketdata.entity.SupportedSymbol;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, UUID> {
    
    List<PriceSnapshot> findBySymbolOrderByFetchedAtDesc(SupportedSymbol symbol);
    
    Optional<PriceSnapshot> findTopBySymbolOrderByFetchedAtDesc(SupportedSymbol symbol);
    
    @Query("SELECT p FROM PriceSnapshot p WHERE p.symbol.symbol = :symbol ORDER BY p.fetchedAt DESC")
    Optional<PriceSnapshot> findLatestBySymbolString(@Param("symbol") String symbol);
}

package com.fininsight.marketdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
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
    
    Optional<PriceSnapshot> findTopBySymbolSymbolOrderByFetchedAtDesc(String symbol);
    
    @org.springframework.data.jpa.repository.Query("""
        SELECT p FROM PriceSnapshot p
        WHERE p.symbol.active = true
          AND p.fetchedAt = (
            SELECT MAX(p2.fetchedAt)
            FROM PriceSnapshot p2
            WHERE p2.symbol = p.symbol
          )
        """)
    List<PriceSnapshot> findLatestSnapshotsForActiveSymbols();
    
    default Optional<PriceSnapshot> findLatestBySymbolString(String symbol) {
        return findTopBySymbolSymbolOrderByFetchedAtDesc(symbol);
    }
}

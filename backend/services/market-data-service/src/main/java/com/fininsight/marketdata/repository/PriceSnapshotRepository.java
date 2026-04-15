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
    
    @Query("""
        SELECT p FROM PriceSnapshot p
        JOIN FETCH p.symbol s
        WHERE s = :symbol
        ORDER BY p.fetchedAt DESC
        """)
    List<PriceSnapshot> findBySymbolOrderByFetchedAtDesc(@Param("symbol") SupportedSymbol symbol);
    
    Optional<PriceSnapshot> findTopBySymbolOrderByFetchedAtDesc(SupportedSymbol symbol);
    
    Optional<PriceSnapshot> findTopBySymbolSymbolOrderByFetchedAtDesc(String symbol);
    
    @Query("""
        SELECT p FROM PriceSnapshot p
        JOIN FETCH p.symbol s
        WHERE s.active = true
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

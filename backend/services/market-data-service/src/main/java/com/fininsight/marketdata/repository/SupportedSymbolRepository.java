package com.fininsight.marketdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fininsight.marketdata.entity.SupportedSymbol;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportedSymbolRepository extends JpaRepository<SupportedSymbol, String> {
    
    List<SupportedSymbol> findByActiveTrue();
    
    Optional<SupportedSymbol> findBySymbolAndActiveTrue(String symbol);
}

package com.fininsight.marketdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.fininsight.marketdata.entity.SupportedSymbol;
import com.fininsight.marketdata.entity.enums.SymbolType;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface SupportedSymbolRepository extends JpaRepository<SupportedSymbol, String> {

    List<SupportedSymbol> findByActiveTrue();

    List<SupportedSymbol> findByActiveTrueAndTypeIn(Collection<SymbolType> types);

    Optional<SupportedSymbol> findBySymbolAndActiveTrue(String symbol);
}

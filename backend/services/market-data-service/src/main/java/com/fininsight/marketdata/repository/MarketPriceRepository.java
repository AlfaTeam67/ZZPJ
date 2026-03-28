package com.fininsight.marketdata.repository;

import com.fininsight.marketdata.entity.MarketPrice;
import com.fininsight.marketdata.entity.Symbol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MarketPriceRepository extends JpaRepository<MarketPrice, Long> {
    
    List<MarketPrice> findBySymbolOrderByTimestampDesc(Symbol symbol);
    
    @Query("SELECT mp FROM MarketPrice mp WHERE mp.timestamp = " +
           "(SELECT MAX(mp2.timestamp) FROM MarketPrice mp2 WHERE mp2.symbol = mp.symbol)")
    List<MarketPrice> findLatestPrices();
}

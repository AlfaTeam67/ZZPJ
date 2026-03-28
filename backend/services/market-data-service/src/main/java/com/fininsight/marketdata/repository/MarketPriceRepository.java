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
    
    @Query("SELECT mp FROM MarketPrice mp WHERE mp.id IN " +
           "(SELECT MAX(mp2.id) FROM MarketPrice mp2 GROUP BY mp2.symbol)")
    List<MarketPrice> findLatestPrices();
}

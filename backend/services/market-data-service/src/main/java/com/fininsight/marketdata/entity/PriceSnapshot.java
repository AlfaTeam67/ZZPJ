package com.fininsight.marketdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "price_snapshots", indexes = {
    @Index(name = "idx_snap_symbol_fetched", columnList = "symbol, fetched_at DESC"),
    @Index(name = "idx_snap_fetched", columnList = "fetched_at DESC")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceSnapshot {
    
    @Id
    @GeneratedValue
    private UUID id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symbol", nullable = false)
    private SupportedSymbol symbol;
    
    @Column(nullable = false, length = 50)
    private String source;
    
    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal price;
    
    @Column(nullable = false, length = 10)
    private String currency;
    
    @Column(name = "change_pct_24h", precision = 8, scale = 4)
    private BigDecimal changePct24h;
    
    @Column(name = "volume_24h", precision = 24, scale = 4)
    private BigDecimal volume24h;
    
    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt;
    
    @PrePersist
    protected void onCreate() {
        if (fetchedAt == null) {
            fetchedAt = Instant.now();
        }
    }
}

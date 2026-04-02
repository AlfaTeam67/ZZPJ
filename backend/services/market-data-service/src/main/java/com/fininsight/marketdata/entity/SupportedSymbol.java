package com.fininsight.marketdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fininsight.marketdata.entity.enums.SymbolType;

import java.time.Instant;

@Entity
@Table(name = "supported_symbols", indexes = {
    @Index(name = "idx_supported_symbols_type", columnList = "type"),
    @Index(name = "idx_supported_symbols_active", columnList = "active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportedSymbol {
    
    @Id
    @Column(length = 20)
    private String symbol;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SymbolType type;
    
    @Column(name = "api_source", nullable = false, length = 50)
    private String apiSource;
    
    @Column(nullable = false)
    private boolean active;
    
    @Column(name = "base_currency", length = 10)
    private String baseCurrency;
    
    @Column(name = "added_at")
    private Instant addedAt;
    
    @PrePersist
    protected void onCreate() {
        if (addedAt == null) {
            addedAt = Instant.now();
        }
    }
}

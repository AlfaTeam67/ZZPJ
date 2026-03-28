package com.fininsight.marketdata.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "market_prices", indexes = {
    @Index(name = "idx_market_price_symbol", columnList = "symbol_id"),
    @Index(name = "idx_market_price_timestamp", columnList = "timestamp")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MarketPrice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Symbol is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symbol_id", nullable = false)
    private Symbol symbol;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal price;
    
    @NotNull(message = "Volume is required")
    @DecimalMin(value = "0.0", message = "Volume must be non-negative")
    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal volume;
    
    @NotNull(message = "Timestamp is required")
    @Column(nullable = false)
    private LocalDateTime timestamp;
}

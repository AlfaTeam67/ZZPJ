package com.fininsight.marketdata.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "symbols", indexes = {
    @Index(name = "idx_symbol_ticker", columnList = "ticker")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Symbol {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Ticker is required")
    @Size(max = 20)
    @Column(unique = true, nullable = false, length = 20)
    private String ticker;
    
    @NotBlank(message = "Name is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String name;
    
    @NotBlank(message = "Type is required")
    @Size(max = 20)
    @Column(nullable = false, length = 20)
    private String type; // STOCK, CRYPTO, FX
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

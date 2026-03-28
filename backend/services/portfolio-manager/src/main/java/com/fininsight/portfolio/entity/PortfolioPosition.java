package com.fininsight.portfolio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "portfolio_positions")
@Getter
@Setter
@ToString(exclude = "portfolio")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    @NotNull(message = "Portfolio is required")
    private Portfolio portfolio;

    @NotBlank(message = "Symbol is required")
    @Column(nullable = false)
    private String symbol;

    @NotNull(message = "Quantity is required")
    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    @NotNull(message = "Average price is required")
    @Column(nullable = false, precision = 19, scale = 4, name = "average_price")
    private BigDecimal averagePrice;

    @NotNull(message = "Current price is required")
    @Column(nullable = false, precision = 19, scale = 4, name = "current_price")
    private BigDecimal currentPrice;

    public BigDecimal getCurrentValue() {
        if (quantity == null || currentPrice == null) {
            return BigDecimal.ZERO;
        }
        return quantity.multiply(currentPrice);
    }
}

package com.fininsight.portfolio.service;

import com.fininsight.portfolio.dto.PortfolioRequest;
import com.fininsight.portfolio.dto.PortfolioResponse;
import com.fininsight.portfolio.entity.Portfolio;
import com.fininsight.portfolio.repository.PortfolioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    @Test
    void shouldReturnAllPortfoliosForUser() {
        Portfolio portfolio = portfolio(1L, "Growth", "user-1", "1000.0000");
        when(portfolioRepository.findByUserId("user-1")).thenReturn(List.of(portfolio));

        List<PortfolioResponse> result = portfolioService.getAllPortfoliosForUser("user-1");

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Growth");
        assertThat(result.getFirst().getUserId()).isEqualTo("user-1");
    }

    @Test
    void shouldReturnPortfolioByIdForUser() {
        Portfolio portfolio = portfolio(7L, "Retirement", "user-1", "15500.0000");
        when(portfolioRepository.findByIdAndUserId(7L, "user-1")).thenReturn(Optional.of(portfolio));

        PortfolioResponse result = portfolioService.getPortfolioById(7L, "user-1");

        assertThat(result.getId()).isEqualTo(7L);
        assertThat(result.getName()).isEqualTo("Retirement");
    }

    @Test
    void shouldThrowNotFoundWhenPortfolioByIdMissing() {
        when(portfolioRepository.findByIdAndUserId(9L, "user-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.getPortfolioById(9L, "user-1"))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404 NOT_FOUND")
            .hasMessageContaining("Portfolio not found");
    }

    @Test
    void shouldCreatePortfolio() {
        PortfolioRequest request = PortfolioRequest.builder()
            .name("Tech")
            .totalValue(new BigDecimal("999.1234"))
            .build();
        Portfolio saved = portfolio(11L, "Tech", "user-77", "999.1234");
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(saved);

        PortfolioResponse result = portfolioService.createPortfolio(request, "user-77");

        assertThat(result.getId()).isEqualTo(11L);
        assertThat(result.getName()).isEqualTo("Tech");
        assertThat(result.getTotalValue()).isEqualByComparingTo("999.1234");
    }

    @Test
    void shouldUpdatePortfolio() {
        Portfolio existing = portfolio(15L, "Old", "user-2", "100.0000");
        Portfolio updated = portfolio(15L, "New", "user-2", "200.0000");
        when(portfolioRepository.findByIdAndUserId(15L, "user-2")).thenReturn(Optional.of(existing));
        when(portfolioRepository.save(existing)).thenReturn(updated);

        PortfolioRequest request = PortfolioRequest.builder()
            .name("New")
            .totalValue(new BigDecimal("200.0000"))
            .build();
        PortfolioResponse result = portfolioService.updatePortfolio(15L, request, "user-2");

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getTotalValue()).isEqualByComparingTo("200.0000");
    }

    @Test
    void shouldDeletePortfolio() {
        Portfolio existing = portfolio(21L, "Delete me", "user-3", "321.0000");
        when(portfolioRepository.findByIdAndUserId(21L, "user-3")).thenReturn(Optional.of(existing));

        portfolioService.deletePortfolio(21L, "user-3");

        verify(portfolioRepository).delete(existing);
    }

    @Test
    void shouldThrowNotFoundWhenDeleteMissing() {
        when(portfolioRepository.findByIdAndUserId(21L, "user-3")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.deletePortfolio(21L, "user-3"))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);

        verify(portfolioRepository, never()).delete(any());
    }

    private static Portfolio portfolio(Long id, String name, String userId, String value) {
        Portfolio portfolio = new Portfolio();
        portfolio.setId(id);
        portfolio.setName(name);
        portfolio.setUserId(userId);
        portfolio.setTotalValue(new BigDecimal(value));
        portfolio.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));
        return portfolio;
    }
}

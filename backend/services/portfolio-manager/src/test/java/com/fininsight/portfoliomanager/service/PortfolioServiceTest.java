package com.fininsight.portfoliomanager.service;

import com.fininsight.portfoliomanager.domain.Portfolio;
import com.fininsight.portfoliomanager.domain.User;
import com.fininsight.portfoliomanager.dto.PortfolioRequest;
import com.fininsight.portfoliomanager.dto.PortfolioResponse;
import com.fininsight.portfoliomanager.repository.AssetRepository;
import com.fininsight.portfoliomanager.repository.PortfolioDataRepository;
import com.fininsight.portfoliomanager.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioDataRepository portfolioRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    @Test
    void shouldReturnAllPortfoliosForUser() {
        UUID userId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID portfolioId = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
        Portfolio portfolio = portfolio(portfolioId, "Growth", userId);
        when(portfolioRepository.findByUserId(userId)).thenReturn(List.of(portfolio));
        when(assetRepository.findTotalValuesByPortfolioIds(List.of(portfolioId)))
            .thenReturn(List.of(projection(portfolioId, "1000.0000")));

        List<PortfolioResponse> result = portfolioService.getAllPortfoliosForUser(userId.toString());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Growth");
        assertThat(result.getFirst().getUserId()).isEqualTo(userId);
        assertThat(result.getFirst().getTotalValue()).isEqualByComparingTo("1000.000000000000");
    }

    @Test
    void shouldReturnPortfolioByIdForUser() {
        UUID userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID portfolioId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        Portfolio portfolio = portfolio(portfolioId, "Retirement", userId);
        when(portfolioRepository.findByIdAndUserId(portfolioId, userId)).thenReturn(Optional.of(portfolio));
        when(assetRepository.calculateTotalValueByPortfolioId(portfolioId)).thenReturn(BigDecimal.ZERO);

        PortfolioResponse result = portfolioService.getPortfolioById(portfolioId, userId.toString());

        assertThat(result.getId()).isEqualTo(portfolioId);
        assertThat(result.getName()).isEqualTo("Retirement");
    }

    @Test
    void shouldThrowNotFoundWhenPortfolioByIdMissing() {
        UUID userId = UUID.fromString("33333333-3333-3333-3333-333333333333");
        UUID portfolioId = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
        when(portfolioRepository.findByIdAndUserId(portfolioId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.getPortfolioById(portfolioId, userId.toString()))
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining("404 NOT_FOUND")
            .hasMessageContaining("Portfolio not found");
    }

    @Test
    void shouldCreatePortfolio() {
        UUID userId = UUID.fromString("44444444-4444-4444-4444-444444444444");
        User user = new User();
        user.setId(userId);
        PortfolioRequest request = PortfolioRequest.builder()
            .name("Tech")
            .description("Long-term")
            .build();
        Portfolio saved = portfolio(UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd"), "Tech", userId);
        saved.setDescription("Long-term");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(saved);

        PortfolioResponse result = portfolioService.createPortfolio(request, userId.toString());

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getName()).isEqualTo("Tech");
        assertThat(result.getDescription()).isEqualTo("Long-term");
        assertThat(result.getTotalValue()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(assetRepository, never()).calculateTotalValueByPortfolioId(saved.getId());
    }

    @Test
    void shouldCreatePortfolioAndProvisionMissingUser() {
        UUID userId = UUID.fromString("48484848-4848-4848-4848-484848484848");
        PortfolioRequest request = PortfolioRequest.builder()
            .name("Autoprovisioned User Portfolio")
            .description("Created without pre-existing user row")
            .build();
        Portfolio saved = portfolio(UUID.fromString("99999999-9999-9999-9999-999999999999"), request.getName(), userId);
        saved.setDescription(request.getDescription());

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(saved);

        PortfolioResponse result = portfolioService.createPortfolio(request, userId.toString());

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getUserId()).isEqualTo(userId);
        verify(userRepository).save(any(User.class));
        assertThat(result.getTotalValue()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(assetRepository, never()).calculateTotalValueByPortfolioId(saved.getId());
    }

    @Test
    void shouldUpdatePortfolio() {
        UUID userId = UUID.fromString("55555555-5555-5555-5555-555555555555");
        UUID portfolioId = UUID.fromString("eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee");
        Portfolio existing = portfolio(portfolioId, "Old", userId);
        Portfolio updated = portfolio(portfolioId, "New", userId);
        updated.setDescription("Updated");
        when(portfolioRepository.findByIdAndUserId(portfolioId, userId)).thenReturn(Optional.of(existing));
        when(portfolioRepository.save(existing)).thenReturn(updated);
        when(assetRepository.calculateTotalValueByPortfolioId(portfolioId)).thenReturn(BigDecimal.ZERO);

        PortfolioRequest request = PortfolioRequest.builder()
            .name("New")
            .description("Updated")
            .build();
        PortfolioResponse result = portfolioService.updatePortfolio(portfolioId, request, userId.toString());

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getDescription()).isEqualTo("Updated");
    }

    @Test
    void shouldDeletePortfolio() {
        UUID userId = UUID.fromString("66666666-6666-6666-6666-666666666666");
        UUID portfolioId = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
        Portfolio existing = portfolio(portfolioId, "Delete me", userId);
        when(portfolioRepository.findByIdAndUserId(portfolioId, userId)).thenReturn(Optional.of(existing));

        portfolioService.deletePortfolio(portfolioId, userId.toString());

        verify(portfolioRepository).delete(existing);
    }

    @Test
    void shouldThrowNotFoundWhenDeleteMissing() {
        UUID userId = UUID.fromString("77777777-7777-7777-7777-777777777777");
        UUID portfolioId = UUID.fromString("12121212-1212-1212-1212-121212121212");
        when(portfolioRepository.findByIdAndUserId(portfolioId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> portfolioService.deletePortfolio(portfolioId, userId.toString()))
            .isInstanceOf(ResponseStatusException.class)
            .extracting(ex -> ((ResponseStatusException) ex).getStatusCode())
            .isEqualTo(HttpStatus.NOT_FOUND);

        verify(portfolioRepository, never()).delete(any());
    }

    private static Portfolio portfolio(UUID id, String name, UUID userId) {
        User user = new User();
        user.setId(userId);

        Portfolio portfolio = new Portfolio();
        portfolio.setId(id);
        portfolio.setName(name);
        portfolio.setUser(user);
        portfolio.setCreatedAt(Instant.parse("2026-01-01T12:00:00Z"));
        return portfolio;
    }

    private static AssetRepository.PortfolioTotalValueProjection projection(UUID portfolioId, String totalValue) {
        return new AssetRepository.PortfolioTotalValueProjection() {
            @Override
            public UUID getPortfolioId() {
                return portfolioId;
            }

            @Override
            public BigDecimal getTotalValue() {
                return new BigDecimal(totalValue);
            }
        };
    }
}

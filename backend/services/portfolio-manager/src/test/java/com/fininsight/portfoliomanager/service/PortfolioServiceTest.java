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
import org.springframework.dao.DataIntegrityViolationException;
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
            .thenReturn(List.of(projection(portfolioId, "USD", "1000.0000")));

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
        when(assetRepository.findTotalValuesByPortfolioId(portfolioId)).thenReturn(List.of());

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
        verify(assetRepository, never()).findTotalValuesByPortfolioId(saved.getId());
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
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(saved);

        PortfolioResponse result = portfolioService.createPortfolio(request, userId.toString());

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getUserId()).isEqualTo(userId);
        verify(userRepository).saveAndFlush(any(User.class));
        assertThat(result.getTotalValue()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(assetRepository, never()).findTotalValuesByPortfolioId(saved.getId());
    }

    @Test
    void shouldRecoverFromUserProvisioningRaceCondition() {
        UUID userId = UUID.fromString("58585858-5858-5858-5858-585858585858");
        User persistedUser = new User();
        persistedUser.setId(userId);
        PortfolioRequest request = PortfolioRequest.builder()
            .name("Race-safe Portfolio")
            .description("Created after concurrent user insert")
            .build();
        Portfolio saved = portfolio(UUID.fromString("68686868-6868-6868-6868-686868686868"), request.getName(), userId);
        saved.setDescription(request.getDescription());

        when(userRepository.findById(userId))
            .thenReturn(Optional.empty())
            .thenReturn(Optional.of(persistedUser));
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate key"));
        when(portfolioRepository.save(any(Portfolio.class))).thenReturn(saved);

        PortfolioResponse result = portfolioService.createPortfolio(request, userId.toString());

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getUserId()).isEqualTo(userId);
        verify(userRepository).saveAndFlush(any(User.class));
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
        when(assetRepository.findTotalValuesByPortfolioId(portfolioId)).thenReturn(List.of());

        PortfolioRequest request = PortfolioRequest.builder()
            .name("New")
            .description("Updated")
            .build();
        PortfolioResponse result = portfolioService.updatePortfolio(portfolioId, request, userId.toString());

        assertThat(result.getName()).isEqualTo("New");
        assertThat(result.getDescription()).isEqualTo("Updated");
    }

    @Test
    void shouldReturnNullTotalValueForMultiCurrencyPortfolioList() {
        UUID userId = UUID.fromString("98989898-9898-9898-9898-989898989898");
        UUID portfolioId = UUID.fromString("10101010-1010-1010-1010-101010101010");
        Portfolio portfolio = portfolio(portfolioId, "Mixed", userId);
        when(portfolioRepository.findByUserId(userId)).thenReturn(List.of(portfolio));
        when(assetRepository.findTotalValuesByPortfolioIds(List.of(portfolioId))).thenReturn(List.of(
            projection(portfolioId, "USD", "100.0000"),
            projection(portfolioId, "EUR", "200.0000")
        ));

        List<PortfolioResponse> result = portfolioService.getAllPortfoliosForUser(userId.toString());

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getTotalValue()).isNull();
    }

    @Test
    void shouldReturnNullTotalValueForMultiCurrencyPortfolioById() {
        UUID userId = UUID.fromString("20202020-2020-2020-2020-202020202020");
        UUID portfolioId = UUID.fromString("30303030-3030-3030-3030-303030303030");
        Portfolio portfolio = portfolio(portfolioId, "Mixed", userId);
        when(portfolioRepository.findByIdAndUserId(portfolioId, userId)).thenReturn(Optional.of(portfolio));
        when(assetRepository.findTotalValuesByPortfolioId(portfolioId)).thenReturn(List.of(
            projection(portfolioId, "USD", "100.0000"),
            projection(portfolioId, "EUR", "200.0000")
        ));

        PortfolioResponse result = portfolioService.getPortfolioById(portfolioId, userId.toString());

        assertThat(result.getTotalValue()).isNull();
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

    private static AssetRepository.PortfolioCurrencyTotalValueProjection projection(UUID portfolioId, String currency, String totalValue) {
        return new AssetRepository.PortfolioCurrencyTotalValueProjection() {
            @Override
            public UUID getPortfolioId() {
                return portfolioId;
            }

            @Override
            public String getCurrency() {
                return currency;
            }

            @Override
            public BigDecimal getTotalValue() {
                return new BigDecimal(totalValue);
            }
        };
    }
}

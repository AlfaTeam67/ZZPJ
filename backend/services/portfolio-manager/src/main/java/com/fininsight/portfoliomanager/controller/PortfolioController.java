package com.fininsight.portfoliomanager.controller;

import com.fininsight.portfoliomanager.dto.portfolio.CreatePortfolioRequest;
import com.fininsight.portfoliomanager.dto.portfolio.PortfolioResponse;
import com.fininsight.portfoliomanager.dto.portfolio.UpdatePortfolioRequest;
import com.fininsight.portfoliomanager.dto.valuation.PortfolioValuationHistoryResponse;
import com.fininsight.portfoliomanager.dto.valuation.PortfolioValuationResponse;
import com.fininsight.portfoliomanager.service.PortfolioService;
import com.fininsight.portfoliomanager.service.PortfolioSharingService;
import com.fininsight.portfoliomanager.service.PortfolioValuationHistoryService;
import com.fininsight.portfoliomanager.service.ValuationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
@Tag(name = "Portfolio", description = "Portfolio management APIs")
@SecurityRequirement(name = "bearerAuth")
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final ValuationService valuationService;
    private final PortfolioValuationHistoryService historyService;
    private final PortfolioSharingService sharingService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get all portfolios for the authenticated user (paginated)")
    public ResponseEntity<Page<PortfolioResponse>> getAllPortfolios(
        @Parameter(description = "0-based page index") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "Sort field and direction, e.g. createdAt,desc") @RequestParam(defaultValue = "createdAt,desc") String sort,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Pageable pageable = buildPageable(page, size, sort);
        return ResponseEntity.ok(portfolioService.getPortfoliosForUserPaged(userId, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get portfolio by ID")
    public ResponseEntity<PortfolioResponse> getPortfolioById(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(portfolioService.getPortfolio(id, userId));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create a new portfolio")
    public ResponseEntity<PortfolioResponse> createPortfolio(
        @Valid @RequestBody CreatePortfolioRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolioService.createPortfolio(request, userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update an existing portfolio")
    public ResponseEntity<PortfolioResponse> updatePortfolio(
        @PathVariable UUID id,
        @Valid @RequestBody UpdatePortfolioRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(portfolioService.updatePortfolio(id, request, userId));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete a portfolio")
    public ResponseEntity<Void> deletePortfolio(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        portfolioService.deletePortfolio(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/valuation")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get portfolio valuation with current market prices")
    public ResponseEntity<PortfolioValuationResponse> getPortfolioValuation(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        var userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(valuationService.valuate(id, userId, jwt.getTokenValue()));
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get portfolio valuation history (daily snapshots)")
    public ResponseEntity<List<PortfolioValuationHistoryResponse>> getPortfolioHistory(
        @PathVariable UUID id,
        @Parameter(description = "Start date (ISO, e.g. 2024-01-01). Defaults to 1 month ago.")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @Parameter(description = "End date (ISO, e.g. 2024-12-31). Defaults to today.")
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        LocalDate fromDate = from != null ? from : LocalDate.now().minusMonths(1);
        LocalDate toDate   = to   != null ? to   : LocalDate.now();
        return ResponseEntity.ok(historyService.getHistory(id, userId, fromDate, toDate));
    }

    @PostMapping("/{id}/share")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Generate a shareable link token for a portfolio")
    public ResponseEntity<java.util.Map<String, String>> generateShareToken(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        String token = sharingService.generateShareToken(id, userId);
        return ResponseEntity.ok(java.util.Map.of(
            "shareToken", token,
            "shareUrl", "/api/public/portfolios/" + token
        ));
    }

    @DeleteMapping("/{id}/share")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Revoke the shareable link token for a portfolio")
    public ResponseEntity<Void> revokeShareToken(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        sharingService.revokeShareToken(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Parsuje prosty format "field,direction" lub "field".
     * Przykłady: "createdAt,desc", "name,asc", "name"
     */
    private Pageable buildPageable(int page, int size, String sort) {
        int clampedSize = Math.max(1, Math.min(size, 100));
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, clampedSize, Sort.by("createdAt").descending());
        }
        String[] parts = sort.split(",", 2);
        String field = parts[0].trim();
        Sort.Direction direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, clampedSize, Sort.by(direction, field));
    }
}

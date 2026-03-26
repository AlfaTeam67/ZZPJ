package com.fininsight.portfolio.controller;

import com.fininsight.portfolio.dto.PortfolioRequest;
import com.fininsight.portfolio.dto.PortfolioResponse;
import com.fininsight.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
@Tag(name = "Portfolio", description = "Portfolio management APIs")
@SecurityRequirement(name = "bearerAuth")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get all portfolios for the authenticated user")
    public ResponseEntity<List<PortfolioResponse>> getAllPortfolios(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        List<PortfolioResponse> portfolios = portfolioService.getAllPortfoliosForUser(userId);
        return ResponseEntity.ok(portfolios);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get portfolio by ID")
    public ResponseEntity<PortfolioResponse> getPortfolioById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        PortfolioResponse portfolio = portfolioService.getPortfolioById(id, userId);
        return ResponseEntity.ok(portfolio);
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Create a new portfolio")
    public ResponseEntity<PortfolioResponse> createPortfolio(
            @Valid @RequestBody PortfolioRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        PortfolioResponse portfolio = portfolioService.createPortfolio(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(portfolio);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update an existing portfolio")
    public ResponseEntity<PortfolioResponse> updatePortfolio(
            @PathVariable Long id,
            @Valid @RequestBody PortfolioRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        PortfolioResponse portfolio = portfolioService.updatePortfolio(id, request, userId);
        return ResponseEntity.ok(portfolio);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Delete a portfolio")
    public ResponseEntity<Void> deletePortfolio(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        portfolioService.deletePortfolio(id, userId);
        return ResponseEntity.noContent().build();
    }
}

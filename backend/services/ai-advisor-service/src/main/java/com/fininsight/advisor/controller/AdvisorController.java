package com.fininsight.advisor.controller;

import com.fininsight.advisor.dto.RecommendationRequest;
import com.fininsight.advisor.dto.RecommendationResponse;
import com.fininsight.advisor.service.AdvisorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "AI Advisor", description = "AI-powered financial recommendations")
@SecurityRequirement(name = "bearer-jwt")
public class AdvisorController {

    private final AdvisorService advisorService;

    @PostMapping
    @Operation(summary = "Generate AI recommendation",
        description = "Generates a portfolio-aware recommendation grounded in recent news headlines. "
            + "Persists the result and returns its id for later retrieval.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recommendation generated"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "502", description = "Portfolio service unavailable"),
        @ApiResponse(responseCode = "503", description = "Recommendation engine unavailable")
    })
    public ResponseEntity<RecommendationResponse> generate(
        @Valid @RequestBody RecommendationRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(advisorService.generateRecommendation(userId, jwt.getTokenValue(), request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get recommendation by id (with news context)")
    public ResponseEntity<RecommendationResponse> getById(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(advisorService.getById(id, userId));
    }

    @GetMapping("/me")
    @Operation(summary = "List recommendations for the authenticated user")
    public ResponseEntity<Page<RecommendationResponse>> listMine(
        @Parameter(description = "0-based page index") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(advisorService.listForUser(userId, page, clampSize(size)));
    }

    @GetMapping("/portfolio/{portfolioId}")
    @Operation(summary = "List recommendations for a specific portfolio (must belong to user)")
    public ResponseEntity<Page<RecommendationResponse>> listForPortfolio(
        @PathVariable UUID portfolioId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(advisorService.listForPortfolio(portfolioId, userId, page, clampSize(size)));
    }

    private int clampSize(int requested) {
        if (requested < 1) return 1;
        return Math.min(requested, 100);
    }
}

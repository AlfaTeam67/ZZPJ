package com.fininsight.portfoliomanager.controller;

import com.fininsight.portfoliomanager.dto.asset.AddAssetRequest;
import com.fininsight.portfoliomanager.dto.asset.AssetResponse;
import com.fininsight.portfoliomanager.service.AssetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}/assets")
@RequiredArgsConstructor
@Tag(name = "Asset", description = "Asset management APIs")
@SecurityRequirement(name = "bearerAuth")
public class AssetController {

    private final AssetService assetService;

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Get assets for a portfolio (paginated)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Assets returned"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found")
    })
    public ResponseEntity<Page<AssetResponse>> getAssets(
        @PathVariable UUID portfolioId,
        @Parameter(description = "0-based page index") @RequestParam(defaultValue = "0") int page,
        @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
        @Parameter(description = "Sort field and direction, e.g. symbol,asc") @RequestParam(defaultValue = "addedAt,desc") String sort,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        Pageable pageable = buildPageable(page, size, sort);
        return ResponseEntity.ok(assetService.getAssetsPaged(portfolioId, userId, pageable));
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Add an asset to a portfolio")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Asset added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or currency mismatch"),
        @ApiResponse(responseCode = "403", description = "Access denied to this portfolio"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found")
    })
    public ResponseEntity<AssetResponse> addAsset(
        @PathVariable UUID portfolioId,
        @Valid @RequestBody AddAssetRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(assetService.addAsset(portfolioId, request, userId));
    }

    @DeleteMapping("/{assetId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Remove an asset from a portfolio")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Asset removed successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied to this portfolio"),
        @ApiResponse(responseCode = "404", description = "Portfolio or asset not found")
    })
    public ResponseEntity<Void> removeAsset(
        @PathVariable UUID portfolioId,
        @PathVariable UUID assetId,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        assetService.removeAsset(portfolioId, assetId, userId);
        return ResponseEntity.noContent().build();
    }

    private Pageable buildPageable(int page, int size, String sort) {
        int clampedSize = Math.max(1, Math.min(size, 100));
        if (sort == null || sort.isBlank()) {
            return PageRequest.of(page, clampedSize, Sort.by("addedAt").descending());
        }
        String[] parts = sort.split(",", 2);
        String field = parts[0].trim();
        Sort.Direction direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
            ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, clampedSize, Sort.by(direction, field));
    }
}

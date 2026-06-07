package com.fininsight.portfoliomanager.controller;

import com.fininsight.portfoliomanager.dto.bulk.BulkAssetRequest;
import com.fininsight.portfoliomanager.dto.bulk.BulkAssetResponse;
import com.fininsight.portfoliomanager.dto.bulk.BulkTransactionRequest;
import com.fininsight.portfoliomanager.dto.bulk.BulkTransactionResponse;
import com.fininsight.portfoliomanager.service.BulkOperationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/portfolios/{portfolioId}")
@RequiredArgsConstructor
@Tag(name = "Bulk Operations", description = "Bulk asset and transaction import APIs")
@SecurityRequirement(name = "bearerAuth")
public class BulkOperationController {

    private final BulkOperationService bulkOperationService;

    @PostMapping("/assets/bulk")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Bulk add assets to portfolio",
        description = "Processes each asset independently. Failed items appear in the errors list; successful ones are returned immediately.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bulk processing complete (check successCount/errorCount)"),
        @ApiResponse(responseCode = "400", description = "Request body invalid (empty list, missing required fields)"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found")
    })
    public ResponseEntity<BulkAssetResponse> bulkAddAssets(
        @PathVariable UUID portfolioId,
        @Valid @RequestBody BulkAssetRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(bulkOperationService.bulkAddAssets(portfolioId, userId, request));
    }

    @PostMapping("/transactions/bulk")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Bulk add transactions to portfolio (e.g. CSV import)",
        description = "Processes each transaction independently. Failed items appear in the errors list.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Bulk processing complete (check successCount/errorCount)"),
        @ApiResponse(responseCode = "400", description = "Request body invalid"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found")
    })
    public ResponseEntity<BulkTransactionResponse> bulkAddTransactions(
        @PathVariable UUID portfolioId,
        @Valid @RequestBody BulkTransactionRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(bulkOperationService.bulkAddTransactions(portfolioId, userId, request));
    }
}

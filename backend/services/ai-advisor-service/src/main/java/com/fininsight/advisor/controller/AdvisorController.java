package com.fininsight.advisor.controller;

import com.fininsight.advisor.dto.RecommendationRequest;
import com.fininsight.advisor.dto.RecommendationResponse;
import com.fininsight.advisor.service.AdvisorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "AI Advisor", description = "AI-powered financial recommendations")
@SecurityRequirement(name = "bearer-jwt")
public class AdvisorController {
    
    private final AdvisorService advisorService;
    
    @PostMapping
    @Operation(summary = "Get AI recommendations", description = "Generate personalized investment recommendations based on risk tolerance")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recommendations generated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<RecommendationResponse> getRecommendations(
            @Valid @RequestBody RecommendationRequest request) {
        log.info("Received recommendation request for user: {}", request.getUserId());
        RecommendationResponse response = advisorService.generateRecommendations(request);
        return ResponseEntity.ok(response);
    }
    
}

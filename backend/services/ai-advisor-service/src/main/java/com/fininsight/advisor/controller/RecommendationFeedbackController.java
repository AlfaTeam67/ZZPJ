package com.fininsight.advisor.controller;

import com.fininsight.advisor.dto.RecommendationFeedbackRequest;
import com.fininsight.advisor.dto.RecommendationFeedbackResponse;
import com.fininsight.advisor.dto.RecommendationFeedbackStats;
import com.fininsight.advisor.service.RecommendationFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recommendations/{id}/feedback")
@RequiredArgsConstructor
@Tag(name = "AI Advisor Feedback", description = "Feedback for AI recommendations")
@SecurityRequirement(name = "bearer-jwt")
public class RecommendationFeedbackController {

    private final RecommendationFeedbackService feedbackService;

    @PostMapping
    @Operation(summary = "Submit feedback (like/dislike) for a recommendation")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Feedback submitted"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "404", description = "Recommendation not found")
    })
    public ResponseEntity<RecommendationFeedbackResponse> submitFeedback(
        @PathVariable UUID id,
        @Valid @RequestBody RecommendationFeedbackRequest request,
        @AuthenticationPrincipal Jwt jwt
    ) {
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(feedbackService.submitFeedback(id, userId, request));
    }

    @GetMapping
    @Operation(summary = "Get all feedback entries for a recommendation")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Feedback list returned"),
        @ApiResponse(responseCode = "404", description = "Recommendation not found")
    })
    public ResponseEntity<List<RecommendationFeedbackResponse>> getFeedback(
        @PathVariable UUID id,
        @AuthenticationPrincipal Jwt jwt
    ) {
        return ResponseEntity.ok(feedbackService.getFeedbackForRecommendation(id));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get feedback statistics (like/dislike ratio)")
    @ApiResponse(responseCode = "200", description = "Stats returned")
    public ResponseEntity<RecommendationFeedbackStats> getStats(@PathVariable UUID id) {
        return ResponseEntity.ok(feedbackService.getStatistics(id));
    }
}

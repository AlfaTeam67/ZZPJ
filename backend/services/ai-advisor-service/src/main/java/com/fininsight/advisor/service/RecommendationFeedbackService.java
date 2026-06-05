package com.fininsight.advisor.service;

import com.fininsight.advisor.dto.RecommendationFeedbackRequest;
import com.fininsight.advisor.dto.RecommendationFeedbackResponse;
import com.fininsight.advisor.dto.RecommendationFeedbackStats;
import com.fininsight.advisor.entity.Recommendation;
import com.fininsight.advisor.entity.RecommendationFeedback;
import com.fininsight.advisor.exception.RecommendationNotFoundException;
import com.fininsight.advisor.repository.RecommendationFeedbackRepository;
import com.fininsight.advisor.repository.RecommendationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecommendationFeedbackService {

    private final RecommendationFeedbackRepository feedbackRepository;
    private final RecommendationRepository recommendationRepository;

    @Transactional
    public RecommendationFeedbackResponse submitFeedback(
        UUID recommendationId,
        UUID userId,
        RecommendationFeedbackRequest request
    ) {
        Recommendation recommendation = recommendationRepository.findById(recommendationId)
            .orElseThrow(() -> new RecommendationNotFoundException("Recommendation " + recommendationId + " not found"));

        // Verify ownership - 404 to avoid leaking existence of other users' data
        if (!recommendation.getUserId().equals(userId)) {
            throw new RecommendationNotFoundException("Recommendation " + recommendationId + " not found");
        }

        RecommendationFeedback feedback = new RecommendationFeedback();
        feedback.setRecommendation(recommendation);
        feedback.setIsPositive(request.isPositive());
        feedback.setComment(request.comment());

        RecommendationFeedback saved = feedbackRepository.save(feedback);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<RecommendationFeedbackResponse> getFeedbackForRecommendation(UUID recommendationId) {
        if (!recommendationRepository.existsById(recommendationId)) {
            throw new RecommendationNotFoundException("Recommendation " + recommendationId + " not found");
        }
        return feedbackRepository.findByRecommendationId(recommendationId)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public RecommendationFeedbackStats getStatistics(UUID recommendationId) {
        if (!recommendationRepository.existsById(recommendationId)) {
            throw new RecommendationNotFoundException("Recommendation " + recommendationId + " not found");
        }
        long total    = feedbackRepository.countByRecommendationId(recommendationId);
        long positive = feedbackRepository.countByRecommendationIdAndIsPositive(recommendationId, true);
        long negative = total - positive;
        double ratio  = total > 0 ? (double) positive / total : 0.0;
        return new RecommendationFeedbackStats(recommendationId, total, positive, negative, ratio);
    }

    private RecommendationFeedbackResponse toResponse(RecommendationFeedback f) {
        return new RecommendationFeedbackResponse(
            f.getId(),
            f.getRecommendation().getId(),
            f.getIsPositive(),
            f.getComment(),
            f.getCreatedAt()
        );
    }
}

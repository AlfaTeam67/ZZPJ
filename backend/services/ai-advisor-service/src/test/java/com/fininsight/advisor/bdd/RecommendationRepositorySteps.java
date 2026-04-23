package com.fininsight.advisor.bdd;

import com.fininsight.advisor.entity.LlmProvider;
import com.fininsight.advisor.entity.Recommendation;
import com.fininsight.advisor.repository.LlmProviderRepository;
import com.fininsight.advisor.repository.RecommendationRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class RecommendationRepositorySteps {

    @Autowired
    private RecommendationRepository recommendationRepository;

    @Autowired
    private LlmProviderRepository llmProviderRepository;

    private UUID userId;
    private UUID portfolioId;
    private LlmProvider llmProvider;
    private Recommendation savedRecommendation;

    @Given("a user with ID {string}")
    public void aUserWithID(String userIdStr) {
        this.userId = UUID.fromString(userIdStr);
    }

    @And("a portfolio with ID {string}")
    public void aPortfolioWithID(String portfolioIdStr) {
        this.portfolioId = UUID.fromString(portfolioIdStr);
    }

    @And("a default LLM provider exists")
    public void aDefaultLLMProviderExists() {
        llmProvider = llmProviderRepository.findFirstByActiveTrueOrderByPriorityAsc()
                .orElseGet(() -> llmProviderRepository.save(LlmProvider.builder()
                        .name("Test Provider")
                        .modelId("test-model")
                        .apiKeyEnv("TEST_API_KEY")
                        .active(true)
                        .priority((short) 1)
                        .addedAt(Instant.now())
                        .build()));
    }

    @When("I save a new recommendation for this user and portfolio")
    public void iSaveANewRecommendationForThisUserAndPortfolio() {
        Recommendation recommendation = Recommendation.builder()
                .userId(userId)
                .portfolioId(portfolioId)
                .llmProvider(llmProvider)
                .promptSummary("Test prompt")
                .llmResponse("Test response")
                .riskScore(new BigDecimal("0.85"))
                .createdAt(Instant.now())
                .build();
        savedRecommendation = recommendationRepository.save(recommendation);
    }

    @Then("I should be able to find it by user ID")
    public void iShouldBeAbleToFindItByUserID() {
        Page<Recommendation> results = recommendationRepository.findByUserId(userId, PageRequest.of(0, 10, org.springframework.data.domain.Sort.by("createdAt").descending()));
        assertThat(results.getContent()).isNotEmpty();
        assertThat(results.getContent().get(0).getId()).isEqualTo(savedRecommendation.getId());
    }

    @And("it should have the correct risk score {double}")
    public void itShouldHaveTheCorrectRiskScore(Double expectedScore) {
        Recommendation found = recommendationRepository.findById(savedRecommendation.getId()).orElseThrow();
        assertThat(found.getRiskScore()).isEqualByComparingTo(BigDecimal.valueOf(expectedScore));
    }
}

package com.fininsight.advisor.bdd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fininsight.advisor.entity.LlmProvider;
import com.fininsight.advisor.entity.Recommendation;
import com.fininsight.advisor.repository.LlmProviderRepository;
import com.fininsight.advisor.repository.RecommendationRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class RecommendationHistorySteps {

    @Autowired private TestRestTemplate http;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private LlmProviderRepository llmProviderRepository;
    @Autowired private RecommendationRepository recommendationRepository;

    private final ScenarioState state = ScenarioState.INSTANCE;

    @Given("a stored recommendation owned by user {string} for portfolio {string} with risk score {double}")
    public void aStoredRecommendation(String userIdStr, String portfolioIdStr, Double riskScore) {
        UUID userId = UUID.fromString(userIdStr);
        UUID portfolioId = UUID.fromString(portfolioIdStr);
        LlmProvider provider = llmProviderRepository.findFirstByActiveTrueOrderByPriorityAsc()
            .orElseGet(() -> llmProviderRepository.save(LlmProvider.builder()
                .name("History Provider").modelId("test/history:free")
                .apiKeyEnv("TEST_API_KEY").active(true).priority((short) 1).addedAt(Instant.now()).build()));

        Recommendation saved = recommendationRepository.save(Recommendation.builder()
            .userId(userId)
            .portfolioId(portfolioId)
            .llmProvider(provider)
            .promptSummary("seeded")
            .llmResponse("Stored summary.\n- bullet one\n- bullet two\nRISK_SCORE=" + riskScore)
            .riskScore(BigDecimal.valueOf(riskScore))
            .createdAt(Instant.now())
            .build());

        state.storedRecommendationId = saved.getId();
        state.portfolioId = portfolioId;
    }

    @When("the user fetches the stored recommendation by id")
    public void theUserFetchesTheStoredRecommendationById() {
        state.lastResponse = http.exchange(
            "/api/recommendations/{id}", HttpMethod.GET,
            new HttpEntity<>(headers(state.userId)),
            String.class, state.storedRecommendationId);
    }

    @Given("another user {string} tries to access the stored recommendation")
    public void anotherUserTriesToAccessTheStoredRecommendation(String otherUserId) {
        state.lastResponse = http.exchange(
            "/api/recommendations/{id}", HttpMethod.GET,
            new HttpEntity<>(headers(UUID.fromString(otherUserId))),
            String.class, state.storedRecommendationId);
    }

    @When("the user lists their recommendations")
    public void theUserListsTheirRecommendations() {
        state.lastResponse = http.exchange(
            "/api/recommendations/me?page=0&size=20", HttpMethod.GET,
            new HttpEntity<>(headers(state.userId)),
            String.class);
    }

    @Then("the recommendation list contains the stored recommendation")
    public void theRecommendationListContainsTheStoredRecommendation() throws Exception {
        var node = objectMapper.readTree(state.lastResponse.getBody());
        var ids = node.get("content").findValuesAsText("id");
        assertThat(ids).contains(state.storedRecommendationId.toString());
    }

    private HttpHeaders headers(UUID userId) {
        HttpHeaders h = new HttpHeaders();
        h.setBearerAuth(userId.toString());
        return h;
    }
}

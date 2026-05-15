package com.fininsight.advisor.bdd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fininsight.advisor.client.PortfolioClient;
import com.fininsight.advisor.client.llm.LlmChatClient;
import com.fininsight.advisor.client.news.FinnhubNewsClient;
import com.fininsight.advisor.client.news.NewsApiClient;
import com.fininsight.advisor.client.news.NewsItem;
import com.fininsight.advisor.dto.external.PortfolioValuationDto;
import com.fininsight.advisor.entity.LlmProvider;
import com.fininsight.advisor.entity.enums.NewsProvider;
import com.fininsight.advisor.entity.enums.NewsSentiment;
import com.fininsight.advisor.exception.LlmUnavailableException;
import com.fininsight.advisor.repository.LlmProviderRepository;
import com.fininsight.advisor.repository.NewsCacheRepository;
import com.fininsight.advisor.repository.RecommendationNewsRepository;
import com.fininsight.advisor.repository.RecommendationRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RecommendationGenerationSteps {

    @Autowired private TestRestTemplate http;
    @Autowired private ObjectMapper objectMapper;

    @Autowired private LlmChatClient llmChatClient;
    @Autowired private PortfolioClient portfolioClient;
    @Autowired private FinnhubNewsClient finnhubNewsClient;
    @Autowired private NewsApiClient newsApiClient;

    @Autowired private LlmProviderRepository llmProviderRepository;
    @Autowired private NewsCacheRepository newsCacheRepository;
    @Autowired private RecommendationRepository recommendationRepository;
    @Autowired private RecommendationNewsRepository recommendationNewsRepository;

    private final ScenarioState state = ScenarioState.INSTANCE;

    @Before
    public void resetMocks() {
        reset(llmChatClient, portfolioClient, finnhubNewsClient, newsApiClient);
        when(newsApiClient.isEnabled()).thenReturn(false);
        when(finnhubNewsClient.isEnabled()).thenReturn(true);
        recommendationNewsRepository.deleteAll();
        recommendationRepository.deleteAll();
        newsCacheRepository.deleteAll();
        state.reset();
    }

    @Given("an authenticated user {string}")
    public void anAuthenticatedUser(String userId) {
        state.userId = UUID.fromString(userId);
    }

    @Given("a default LLM provider exists")
    public void aDefaultLLMProviderExists() {
        if (llmProviderRepository.findFirstByActiveTrueOrderByPriorityAsc().isEmpty()) {
            llmProviderRepository.save(LlmProvider.builder()
                .name("Primary Test Provider")
                .modelId("test/primary:free")
                .apiKeyEnv("TEST_API_KEY")
                .active(true)
                .priority((short) 1)
                .addedAt(Instant.now())
                .build());
        }
    }

    @Given("the portfolio {string} contains:")
    public void thePortfolioContains(String portfolioIdStr, DataTable holdings) {
        UUID portfolioId = UUID.fromString(portfolioIdStr);
        state.portfolioId = portfolioId;

        List<PortfolioValuationDto.AssetValuationDto> assets = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, String> row : holdings.asMaps()) {
            BigDecimal qty = new BigDecimal(row.get("quantity"));
            BigDecimal avg = new BigDecimal(row.get("avgBuyPrice"));
            BigDecimal cur = new BigDecimal(row.get("currentPrice"));
            BigDecimal value = qty.multiply(cur);
            total = total.add(value);
            assets.add(new PortfolioValuationDto.AssetValuationDto(
                row.get("symbol"), row.get("type"), qty, avg, cur, value,
                value.subtract(qty.multiply(avg)), null));
        }
        PortfolioValuationDto valuation = new PortfolioValuationDto(portfolioId, total, assets, Instant.now());
        when(portfolioClient.getValuation(eq(portfolioId), anyString())).thenReturn(valuation);
    }

    @Given("Finnhub returns the following headlines:")
    public void finnhubReturnsTheFollowingHeadlines(DataTable headlines) {
        Map<String, List<NewsItem>> grouped = new HashMap<>();
        int idCounter = 0;
        for (Map<String, String> row : headlines.asMaps()) {
            String symbol = row.get("symbol");
            grouped.computeIfAbsent(symbol, k -> new ArrayList<>()).add(new NewsItem(
                "ext-" + (idCounter++),
                row.get("headline"),
                "Reuters",
                "https://example.com/" + idCounter,
                symbol,
                NewsProvider.FINNHUB,
                NewsSentiment.NEUTRAL,
                Instant.now()
            ));
        }
        when(finnhubNewsClient.fetchCompanyNews(anyString(), any(), any(), anyInt()))
            .thenAnswer(inv -> grouped.getOrDefault(inv.getArgument(0), List.of()));
    }

    @Given("the LLM responds with:")
    public void theLLMRespondsWith(String content) {
        when(llmChatClient.complete(anyString(), any()))
            .thenReturn(new LlmChatClient.LlmCompletion(content, "test/primary:free", 100, 50));
    }

    @Given("the primary LLM provider is unreachable")
    public void thePrimaryLLMProviderIsUnreachable() {
        // Upewnij się, że mamy dwa providery z różnym priorytetem.
        if (llmProviderRepository.count() < 2) {
            llmProviderRepository.save(LlmProvider.builder()
                .name("Secondary Test Provider").modelId("test/secondary:free")
                .apiKeyEnv("TEST_API_KEY").active(true).priority((short) 2).addedAt(Instant.now()).build());
        }
        state.secondaryModelId = "test/secondary:free";
    }

    @Given("the secondary LLM responds with:")
    public void theSecondaryLLMRespondsWith(String content) {
        // Pierwszy call (primary) - rzuć błąd, drugi (secondary) - zwróć kontent.
        when(llmChatClient.complete(eq("test/primary:free"), any()))
            .thenThrow(new LlmUnavailableException("primary down"));
        when(llmChatClient.complete(eq("test/secondary:free"), any()))
            .thenReturn(new LlmChatClient.LlmCompletion(content, "test/secondary:free", 100, 50));
    }

    @When("the user requests a recommendation for the portfolio with risk {word} and horizon {word}")
    public void theUserRequestsARecommendation(String risk, String horizon) {
        Map<String, Object> body = Map.of(
            "portfolioId", state.portfolioId.toString(),
            "riskTolerance", risk,
            "investmentHorizon", horizon
        );
        state.lastResponse = http.exchange(
            "/api/recommendations", HttpMethod.POST,
            new HttpEntity<>(body, headers(state.userId)),
            String.class);
    }

    @When("the user posts an invalid recommendation request")
    public void theUserPostsAnInvalidRecommendationRequest() {
        Map<String, Object> body = Map.of(
            "riskTolerance", "MODERATE",
            "investmentHorizon", "MID_TERM"
            // intentionally no portfolioId
        );
        state.lastResponse = http.exchange(
            "/api/recommendations", HttpMethod.POST,
            new HttpEntity<>(body, headers(state.userId)),
            String.class);
    }

    @Then("the response status is {int}")
    public void theResponseStatusIs(int status) {
        assertThat(state.lastResponse.getStatusCode().value()).isEqualTo(status);
    }

    @Then("the response contains {int} bullet points")
    public void theResponseContainsBulletPoints(int count) throws Exception {
        var node = objectMapper.readTree(state.lastResponse.getBody());
        assertThat(node.get("bulletPoints").size()).isEqualTo(count);
    }

    @Then("the response risk score is {double}")
    public void theResponseRiskScoreIs(double expected) throws Exception {
        var node = objectMapper.readTree(state.lastResponse.getBody());
        assertThat(node.get("riskScore").decimalValue())
            .isEqualByComparingTo(BigDecimal.valueOf(expected));
    }

    @Then("the response references at least {int} news item")
    public void theResponseReferencesAtLeastOneNewsItem(int min) throws Exception {
        var node = objectMapper.readTree(state.lastResponse.getBody());
        assertThat(node.get("newsContext").size()).isGreaterThanOrEqualTo(min);
    }

    @Then("a recommendation row is persisted for the user")
    public void aRecommendationRowIsPersistedForTheUser() {
        assertThat(recommendationRepository.findAll())
            .anySatisfy(r -> {
                assertThat(r.getUserId()).isEqualTo(state.userId);
                assertThat(r.getPortfolioId()).isEqualTo(state.portfolioId);
            });
    }

    @Then("the response model id is the secondary provider's model")
    public void theResponseModelIdIsTheSecondaryProvidersModel() throws Exception {
        var node = objectMapper.readTree(state.lastResponse.getBody());
        assertThat(node.get("modelId").asText()).isEqualTo(state.secondaryModelId);
        verify(llmChatClient, atLeastOnce()).complete(eq("test/primary:free"), any());
        verify(llmChatClient).complete(eq("test/secondary:free"), any());
    }

    private HttpHeaders headers(UUID userId) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.setBearerAuth(userId.toString());
        return h;
    }
}

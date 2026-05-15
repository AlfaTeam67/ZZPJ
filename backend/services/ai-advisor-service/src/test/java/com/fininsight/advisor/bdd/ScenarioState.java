package com.fininsight.advisor.bdd;

import org.springframework.http.ResponseEntity;

import java.util.UUID;

/**
 * Singleton trzymający stan w obrębie pojedynczego scenariusza Cucumber.
 * Cucumber tworzy nową instancję klasy step definitions na każdy scenariusz,
 * więc współdzielony stan między wieloma klasami stepów wymaga zewnętrznego nośnika.
 */
public final class ScenarioState {

    public static final ScenarioState INSTANCE = new ScenarioState();

    public UUID userId;
    public UUID portfolioId;
    public UUID storedRecommendationId;
    public String secondaryModelId;
    public ResponseEntity<String> lastResponse;

    private ScenarioState() {}

    public void reset() {
        userId = null;
        portfolioId = null;
        storedRecommendationId = null;
        secondaryModelId = null;
        lastResponse = null;
    }
}

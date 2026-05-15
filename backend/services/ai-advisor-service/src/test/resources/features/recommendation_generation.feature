Feature: AI recommendation generation
  In order to act on portfolio insights
  As an authenticated user
  I want the advisor to generate a news-grounded recommendation for my portfolio

  Background:
    Given an authenticated user "f47ac10b-58cc-4372-a567-0e02b2c3d479"
    And a default LLM provider exists
    And the portfolio "550e8400-e29b-41d4-a716-446655440000" contains:
      | symbol | type  | quantity | avgBuyPrice | currentPrice |
      | NVDA   | STOCK | 5        | 400.00      | 500.00       |
      | AAPL   | STOCK | 10       | 150.00      | 180.00       |

  Scenario: Generates a recommendation grounded in news for portfolio holdings
    Given Finnhub returns the following headlines:
      | symbol | headline                                         |
      | NVDA   | NVIDIA reports record quarterly earnings         |
      | AAPL   | Apple announces new product launch in Q3         |
    And the LLM responds with:
      """
      The portfolio is well positioned for upside given recent earnings momentum.
      - Hold NVDA position
      - Take partial profits on AAPL
      - Consider AI-sector diversification
      RISK_SCORE=5.50
      """
    When the user requests a recommendation for the portfolio with risk MODERATE and horizon MID_TERM
    Then the response status is 200
    And the response contains 3 bullet points
    And the response risk score is 5.50
    And the response references at least 1 news item
    And a recommendation row is persisted for the user

  Scenario: Falls back to the next provider when the primary LLM fails
    Given the primary LLM provider is unreachable
    And the secondary LLM responds with:
      """
      Defensive positioning recommended due to provider failover.
      - Maintain current allocation
      - Avoid leverage
      - Increase cash buffer
      RISK_SCORE=3.00
      """
    When the user requests a recommendation for the portfolio with risk LOW and horizon LONG_TERM
    Then the response status is 200
    And the response model id is the secondary provider's model

  Scenario: Returns 400 when portfolioId is missing
    When the user posts an invalid recommendation request
    Then the response status is 400

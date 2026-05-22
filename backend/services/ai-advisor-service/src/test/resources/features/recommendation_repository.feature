Feature: Recommendation Repository

  Scenario: Save and retrieve recommendations
    Given a user with ID "f47ac10b-58cc-4372-a567-0e02b2c3d479"
    And a portfolio with ID "550e8400-e29b-41d4-a716-446655440000"
    And a default LLM provider for repository test exists
    When I save a new recommendation for this user and portfolio
    Then I should be able to find it by user ID
    And it should have the correct risk score 0.85

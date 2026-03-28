Feature: AI Advisor basic smoke

  Scenario: Service status model returns UP
    When AI advisor health payload is prepared
    Then status should be UP


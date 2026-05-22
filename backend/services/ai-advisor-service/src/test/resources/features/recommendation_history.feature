Feature: Recommendation history access control
  Recommendations belong to the user that requested them. Listing and reading
  must respect this even when ids are guessed.

  Background:
    Given an authenticated user "f47ac10b-58cc-4372-a567-0e02b2c3d479"
    And a default LLM provider exists
    And a stored recommendation owned by user "f47ac10b-58cc-4372-a567-0e02b2c3d479" for portfolio "550e8400-e29b-41d4-a716-446655440000" with risk score 6.50

  Scenario: User retrieves their own recommendation by id
    When the user fetches the stored recommendation by id
    Then the response status is 200
    And the response risk score is 6.50

  Scenario: User cannot read another user's recommendation
    Given another user "11111111-1111-1111-1111-111111111111" tries to access the stored recommendation
    Then the response status is 404

  Scenario: User can list their own recommendations
    When the user lists their recommendations
    Then the response status is 200
    And the recommendation list contains the stored recommendation

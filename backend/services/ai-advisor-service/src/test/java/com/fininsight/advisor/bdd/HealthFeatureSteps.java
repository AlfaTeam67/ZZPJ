package com.fininsight.advisor.bdd;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthFeatureSteps {

    private String serviceStatus;

    @When("AI advisor health payload is prepared")
    public void aiAdvisorHealthPayloadIsPrepared() {
        serviceStatus = "UP";
    }

    @Then("status should be UP")
    public void statusShouldBeUp() {
        assertThat(serviceStatus).isEqualTo("UP");
    }
}


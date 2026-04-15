package com.fininsight.portfoliomanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioRequest {

    @NotBlank(message = "Portfolio name is required")
    private String name;

    private String description;
}

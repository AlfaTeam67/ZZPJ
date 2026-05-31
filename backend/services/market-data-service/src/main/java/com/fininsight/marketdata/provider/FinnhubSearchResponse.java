package com.fininsight.marketdata.provider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FinnhubSearchResponse {
    private int count;
    private List<Result> result;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private String description;
        private String displaySymbol;
        private String symbol;
        private String type;
    }
}

package com.fininsight.advisor.client.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fininsight.advisor.exception.LlmUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;

/**
 * Klient OpenRoutera. Endpoint /api/v1/chat/completions jest zgodny z OpenAI,
 * więc gdy w przyszłości zechcemy migrować na inne API, wystarczy podmienić baseUrl.
 *
 * Zalecane przez OpenRouter nagłówki HTTP-Referer i X-Title są przekazywane,
 * żeby request był poprawnie atrybuowany do naszej aplikacji.
 */
@Slf4j
@Component
public class OpenRouterClient implements LlmChatClient {

    private final RestClient restClient;
    private final String apiKey;
    private final double temperature;
    private final int maxTokens;

    public OpenRouterClient(
        @Qualifier("externalRestClientBuilder") RestClient.Builder builder,
        @Value("${app.openrouter.base-url:https://openrouter.ai/api/v1}") String baseUrl,
        @Value("${app.openrouter.api-key:${OPENROUTER_API_KEY:}}") String apiKey,
        @Value("${app.openrouter.referer:https://fin-insight.dev}") String referer,
        @Value("${app.openrouter.app-title:Fin-Insight}") String title,
        @Value("${app.openrouter.temperature:0.4}") double temperature,
        @Value("${app.openrouter.max-tokens:800}") int maxTokens
    ) {
        this.apiKey = apiKey;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.restClient = builder
            .baseUrl(baseUrl)
            .defaultHeader("HTTP-Referer", referer)
            .defaultHeader("X-Title", title)
            .build();
    }

    @Override
    public LlmCompletion complete(String modelId, List<ChatMessage> messages) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new LlmUnavailableException("OPENROUTER_API_KEY is not configured");
        }

        var payload = Map.of(
            "model", modelId,
            "messages", messages.stream().map(m -> Map.of("role", m.role(), "content", m.content())).toList(),
            "temperature", temperature,
            "max_tokens", maxTokens
        );

        try {
            ChatCompletionResponse response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .body(payload)
                .retrieve()
                .body(ChatCompletionResponse.class);

            if (response == null || response.choices == null || response.choices.isEmpty()) {
                throw new LlmUnavailableException("OpenRouter returned empty response for model " + modelId);
            }
            String content = response.choices.get(0).message != null
                ? response.choices.get(0).message.content
                : null;
            if (content == null || content.isBlank()) {
                throw new LlmUnavailableException("OpenRouter returned empty content for model " + modelId);
            }
            Integer prompt = response.usage != null ? response.usage.promptTokens : null;
            Integer completion = response.usage != null ? response.usage.completionTokens : null;
            return new LlmCompletion(content, modelId, prompt, completion);
        } catch (RestClientResponseException e) {
            log.warn("OpenRouter returned {} for model {}: {}", e.getStatusCode(), modelId, e.getResponseBodyAsString());
            throw new LlmUnavailableException("OpenRouter " + e.getStatusCode() + " for model " + modelId, e);
        } catch (Exception e) {
            log.warn("OpenRouter unreachable for model {}: {}", modelId, e.getMessage());
            throw new LlmUnavailableException("OpenRouter unreachable for model " + modelId, e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChatCompletionResponse {
        public List<Choice> choices;
        public Usage usage;

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Choice {
            public Message message;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Message {
            public String role;
            public String content;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Usage {
            @com.fasterxml.jackson.annotation.JsonProperty("prompt_tokens")
            public Integer promptTokens;
            @com.fasterxml.jackson.annotation.JsonProperty("completion_tokens")
            public Integer completionTokens;
            @com.fasterxml.jackson.annotation.JsonProperty("total_tokens")
            public Integer totalTokens;
        }
    }
}

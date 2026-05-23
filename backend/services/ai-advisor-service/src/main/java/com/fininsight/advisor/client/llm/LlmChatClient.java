package com.fininsight.advisor.client.llm;

import java.util.List;

/**
 * Abstrakcja LLM. Pozwala podmienić providera (OpenRouter/OpenAI/Anthropic/Ollama)
 * bez zmian w warstwie biznesowej.
 */
public interface LlmChatClient {

    LlmCompletion complete(String modelId, List<ChatMessage> messages);

    record ChatMessage(String role, String content) {
        public static ChatMessage system(String content) { return new ChatMessage("system", content); }
        public static ChatMessage user(String content)   { return new ChatMessage("user", content); }
    }

    record LlmCompletion(String content, String modelId, Integer promptTokens, Integer completionTokens) {}
}

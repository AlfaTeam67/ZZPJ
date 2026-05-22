package com.fininsight.advisor.service;

import com.fininsight.advisor.client.llm.LlmChatClient;
import com.fininsight.advisor.entity.LlmProvider;
import com.fininsight.advisor.exception.LlmUnavailableException;
import com.fininsight.advisor.repository.LlmProviderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Wybiera aktywnego providera z najwyższym priorytetem i dzwoni do LLM.
 * Przy błędzie idzie po kolejnych providerach (po priority ASC). Dzięki temu
 * jeśli jeden free model jest chwilowo zatkany, dostajemy odpowiedź z drugiego.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LlmInvocationService {

    private final LlmProviderRepository llmProviderRepository;
    private final LlmChatClient chatClient;

    public LlmInvocationResult invoke(List<LlmChatClient.ChatMessage> messages) {
        List<LlmProvider> providers = llmProviderRepository.findAll(Sort.by("priority").ascending())
            .stream()
            .filter(LlmProvider::isActive)
            .toList();

        if (providers.isEmpty()) {
            throw new LlmUnavailableException("No active LLM providers configured");
        }

        LlmUnavailableException lastException = null;
        for (LlmProvider provider : providers) {
            try {
                LlmChatClient.LlmCompletion completion = chatClient.complete(provider.getModelId(), messages);
                return new LlmInvocationResult(provider, completion);
            } catch (LlmUnavailableException ex) {
                log.warn("Provider {} ({}) failed: {}", provider.getName(), provider.getModelId(), ex.getMessage());
                lastException = ex;
            }
        }
        throw new LlmUnavailableException("All LLM providers failed", lastException);
    }

    public record LlmInvocationResult(LlmProvider provider, LlmChatClient.LlmCompletion completion) {}
}

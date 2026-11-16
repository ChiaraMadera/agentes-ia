package org.example.agents.texto;

import org.example.agents.IAgent;
import org.example.client.LlmClient;
import org.example.prompts.texto.TranslatorTextPrompt;

import java.util.Objects;

/**
 * Agente que traduce un texto al castellano.
 */
public class TranslatorTextAgent implements IAgent {

    private final LlmClient llmClient;

    public TranslatorTextAgent(LlmClient llmClient) {
        this.llmClient = Objects.requireNonNull(llmClient, "llmClient");
    }

    @Override
    public String execute(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("La entrada no puede estar vacía");
        }
        return llmClient.generate(TranslatorTextPrompt.build(text));
    }
}

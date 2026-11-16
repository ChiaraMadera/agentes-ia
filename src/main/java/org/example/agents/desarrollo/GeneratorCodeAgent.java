package org.example.agents.desarrollo;

import org.example.agents.IAgent;
import org.example.client.LlmClient;
import org.example.prompts.desarrollo.GeneratorCodePrompt;

import java.util.Objects;

/**
 * Agente que genera código a partir de una especificación en lenguaje natural.
 */
public class GeneratorCodeAgent implements IAgent {

    private final LlmClient llmClient;

    public GeneratorCodeAgent(LlmClient llmClient) {
        this.llmClient = Objects.requireNonNull(llmClient, "llmClient");
    }

    @Override
    public String execute(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("La entrada no puede estar vacía");
        }
        return llmClient.generate(GeneratorCodePrompt.build(text));
    }

    @Override
    public String executeStreaming(String text, java.util.function.Consumer<String> onChunk) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("La entrada no puede estar vacía");
        }
        return llmClient.generateStreaming(GeneratorCodePrompt.build(text), onChunk);
    }
}

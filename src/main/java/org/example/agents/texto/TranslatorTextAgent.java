package org.example.agents.texto;

import org.example.agents.IAgent;
import org.example.client.LLMClient;
import org.example.prompts.texto.TranslatorTextPrompt;

public class TranslatorTextAgent implements IAgent {

    private final LLMClient llmClient;

    public TranslatorTextAgent(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public String execute(String text) throws Exception {
        return llmClient.generate(
                TranslatorTextPrompt.build(text)
        ) ;
    }
}

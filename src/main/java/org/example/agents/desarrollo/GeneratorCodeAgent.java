package org.example.agents.desarrollo;

import org.example.agents.IAgent;
import org.example.client.LLMClient;
import org.example.prompts.desarrollo.GeneratorCodePrompt;

public class GeneratorCodeAgent implements IAgent {

    private final LLMClient llmClient;

    public GeneratorCodeAgent(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    @Override
    public String execute(String text){
        return llmClient.generate(
                GeneratorCodePrompt.build(text)
        );
    }
}

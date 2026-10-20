package org.example.factory;

import org.example.agents.IAgent;
import org.example.agents.desarrollo.GeneratorCodeAgent;
import org.example.agents.texto.TranslatorTextAgent;
import org.example.client.LLMClient;

public class AgentFactory {
    private static final LLMClient CLIENT = new LLMClient();

    public static IAgent create(int option){
        return switch (option){
            case 1 -> new GeneratorCodeAgent(CLIENT);
            case 2 -> new TranslatorTextAgent(CLIENT);
            default -> throw new IllegalArgumentException("Agente no valido");
        };
    }
}

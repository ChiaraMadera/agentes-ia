package org.example.agents;

import org.example.agents.desarrollo.GeneratorCodeAgent;
import org.example.agents.texto.TranslatorTextAgent;
import org.example.client.LlmClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AgentTest {

    private static LlmClient recordingClient(String expectedPromptContains, String response) {
        return prompt -> {
            if (!prompt.contains(expectedPromptContains)) {
                throw new AssertionError("El prompt no contiene: " + expectedPromptContains);
            }
            return response;
        };
    }

    @Test
    void generadorDelegaPromptYDevuelveRespuesta() {
        LlmClient client = recordingClient("Genera el codigo", "public class Foo {}");
        GeneratorCodeAgent agent = new GeneratorCodeAgent(client);

        assertEquals("public class Foo {}", agent.execute("haz una clase Foo"));
    }

    @Test
    void traductorDelegaPromptYDevuelveRespuesta() {
        LlmClient client = recordingClient("Traduce", "Hola mundo");
        TranslatorTextAgent agent = new TranslatorTextAgent(client);

        assertEquals("Hola mundo", agent.execute("Hello world"));
    }

    @Test
    void agentePropagaFallaDelCliente() {
        LlmClient client = prompt -> { throw new RuntimeException("boom"); };
        GeneratorCodeAgent agent = new GeneratorCodeAgent(client);

        assertThrows(RuntimeException.class, () -> agent.execute("x"));
    }

    @Test
    void agenteRechazaClienteNull() {
        assertThrows(NullPointerException.class, () -> new GeneratorCodeAgent(null));
        assertThrows(NullPointerException.class, () -> new TranslatorTextAgent(null));
    }
}
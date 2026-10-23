package org.example.agents;

import org.example.client.LlmClientException;

/**
 * Contrato común de todos los agentes.
 *
 * <p>No declara excepciones checked: la única falla esperable es
 * {@link LlmClientException}, que la UI muestra tal cual.
 */
public interface IAgent {

    /**
     * Ejecuta el agente sobre un texto de entrada.
     *
     * @param text entrada del usuario
     * @return texto generado por el LLM
     * @throws LlmClientException si la llamada al modelo falla
     */
    String execute(String text);
}

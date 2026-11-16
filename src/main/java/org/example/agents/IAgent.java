package org.example.agents;

import org.example.client.LlmClientException;

import java.util.function.Consumer;

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
     * @param text entrada del usuario; no puede ser {@code null} ni estar en blanco
     * @return texto generado por el LLM
     * @throws LlmClientException si la llamada al modelo falla
     * @throws IllegalArgumentException si {@code text} es {@code null} o está en blanco
     */
    String execute(String text);

    /**
     * Ejecuta el agente entregando la respuesta por fragmentos a medida que llega.
     *
     * <p>La implementación por defecto delega en {@link #execute(String)} y entrega
     * el resultado completo como un único fragmento.
     *
     * @param text    entrada del usuario
     * @param onChunk consumidor de cada fragmento (puede ser {@code null})
     * @return texto completo generado por el LLM
     */
    default String executeStreaming(String text, Consumer<String> onChunk) {
        String result = execute(text);
        if (onChunk != null) {
            onChunk.accept(result);
        }
        return result;
    }
}

package org.example.client;

import org.example.client.LlmClientException;

import java.util.function.Consumer;

/**
 * Puerto de salida hacia un modelo de lenguaje.
 *
 * <p>Los agentes dependen de esta interfaz y no de la implementación HTTP concreta,
 * lo que permite reemplazarla por un doble de prueba sin levantar Ollama.
 */
public interface LlmClient {

    /**
     * Envía un prompt y devuelve la respuesta completa del modelo.
     *
     * @param prompt texto de entrada ya construido por el agente
     * @return texto generado por el modelo
     * @throws LlmClientException si la llamada falla o la respuesta no es utilizable
     */
    String generate(String prompt);

    /**
     * Envía un prompt y entrega la respuesta por fragmentos a medida que llega.
     *
     * <p>La implementación por defecto delega en {@link #generate(String)} y entrega
     * el resultado completo como un único fragmento, de modo que los dobles de prueba
     * no necesiten implementar streaming.
     *
     * @param prompt   texto de entrada ya construido por el agente
     * @param onChunk  consumidor de cada fragmento recibido (puede ser {@code null})
     * @return texto completo generado por el modelo
     * @throws LlmClientException si la llamada falla o la respuesta no es utilizable
     */
    default String generateStreaming(String prompt, Consumer<String> onChunk) {
        String result = generate(prompt);
        if (onChunk != null) {
            onChunk.accept(result);
        }
        return result;
    }
}
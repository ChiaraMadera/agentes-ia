package org.example.client;

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
}

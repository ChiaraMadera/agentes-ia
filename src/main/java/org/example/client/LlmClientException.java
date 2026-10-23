package org.example.client;

/**
 * Falla al comunicarse con el LLM o al interpretar su respuesta.
 *
 * <p>Es unchecked a propósito: el agente no puede hacer nada útil más que dejar que
 * la UI muestre el mensaje. Antes esta situación se representaba devolviendo el texto
 * {@code "Error: ..."}, que era indistinguible de una respuesta real del modelo.
 */
public class LlmClientException extends RuntimeException {

    public LlmClientException(String message) {
        super(message);
    }

    public LlmClientException(String message, Throwable cause) {
        super(message, cause);
    }
}

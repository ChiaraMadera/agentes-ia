package org.example.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Data Transfer Object para respuestas de la API de LLM.
 *
 * <p>{@code error} sólo viene informado cuando Ollama rechaza la petición
 * (por ejemplo, un modelo que no está descargado).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResponseDTO(
        String response,
        String error
) {
}

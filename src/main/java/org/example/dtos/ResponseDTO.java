package org.example.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Data Transfer Objetct para respuestas de la API de LLM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ResponseDTO(
        String response
) {
}

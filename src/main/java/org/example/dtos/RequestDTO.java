package org.example.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Data Transfer Objetc para peticiones a la API de LLM
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RequestDTO(
        String model,
        String prompt,
        boolean stream
) {
}

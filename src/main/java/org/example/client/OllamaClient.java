package org.example.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.Config;
import org.example.dtos.RequestDTO;
import org.example.dtos.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;

/**
 * Implementación de {@link LlmClient} contra la API de generación de Ollama.
 *
 * <p>Tres responsabilidades que antes estaban mezcladas y ahora son explícitas:
 * validar el status HTTP, respetar un timeout y distinguir una falla de una respuesta
 * real del modelo.
 */
public class OllamaClient implements LlmClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(OllamaClient.class);
    private static final int MAX_BODY_PREVIEW = 200;

    private final Config config;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public OllamaClient(Config config) {
        this(
                config,
                HttpClient.newBuilder()
                        .connectTimeout(config.getConnectTimeout())
                        .build(),
                new ObjectMapper()
        );
    }

    /**
     * Constructor para pruebas: permite inyectar un {@link HttpClient} apuntado a un
     * servidor falso y así ejercitar respuestas de error, timeout o body inválido.
     */
    OllamaClient(Config config, HttpClient httpClient, ObjectMapper mapper) {
        this.config = config;
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    @Override
    public String generate(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            throw new LlmClientException("El prompt está vacío");
        }

        HttpRequest request = buildRequest(prompt);
        long startedAt = System.nanoTime();
        HttpResponse<String> response = send(request);
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;

        LOGGER.info("Ollama respondió {} en {} ms con el modelo {}",
                response.statusCode(), elapsedMs, config.getModel());

        if (response.statusCode() / 100 != 2) {
            String detail = describeError(response.body());
            LOGGER.warn("Ollama rechazó la petición (HTTP {}): {}", response.statusCode(), detail);
            throw new LlmClientException(
                    "Ollama rechazó la petición (HTTP " + response.statusCode() + "): " + detail);
        }

        return extractResponse(response.body());
    }

    private HttpRequest buildRequest(String prompt) {
        RequestDTO body = new RequestDTO(config.getModel(), prompt, false);

        final String json;
        try {
            json = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new LlmClientException("No se pudo serializar la petición al LLM", e);
        }

        return HttpRequest.newBuilder()
                .uri(URI.create(config.getUrl()))
                .timeout(config.getRequestTimeout())
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (HttpTimeoutException e) {
            throw new LlmClientException(
                    "Ollama no respondió en " + config.getRequestTimeout().toSeconds()
                            + " s (modelo " + config.getModel() + ")", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LlmClientException("La llamada al LLM fue interrumpida", e);
        } catch (IOException e) {
            throw new LlmClientException(
                    "No se pudo conectar con Ollama en " + config.getUrl()
                            + "; verificá que el servicio esté corriendo", e);
        }
    }

    private String extractResponse(String body) {
        ResponseDTO result = parse(body);
        if (result.response() == null || result.response().isBlank()) {
            throw new LlmClientException("Ollama devolvió una respuesta vacía");
        }
        return result.response();
    }

    private ResponseDTO parse(String body) {
        try {
            return mapper.readValue(body, ResponseDTO.class);
        } catch (JsonProcessingException e) {
            throw new LlmClientException(
                    "La respuesta del LLM no es JSON válido: " + preview(body), e);
        }
    }

    /**
     * Ollama responde los errores como {@code {"error": "..."}}. Si el body no es JSON
     * (por ejemplo, un proxy devolviendo HTML) se muestra un fragmento crudo.
     */
    private String describeError(String body) {
        try {
            ResponseDTO error = mapper.readValue(body, ResponseDTO.class);
            if (error.error() != null && !error.error().isBlank()) {
                return error.error();
            }
        } catch (JsonProcessingException ignored) {
            // El cuerpo del error no es JSON: se usa el preview crudo.
        }
        return preview(body);
    }

    private static String preview(String body) {
        if (body == null || body.isBlank()) {
            return "<cuerpo vacío>";
        }
        String trimmed = body.strip();
        return trimmed.length() <= MAX_BODY_PREVIEW
                ? trimmed
                : trimmed.substring(0, MAX_BODY_PREVIEW) + "…";
    }
}

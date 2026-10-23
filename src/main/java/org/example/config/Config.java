package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Properties;

/**
 * Configuración de la conexión con el LLM, cargada desde {@code application.properties}.
 *
 * <p>La validación ocurre al cargar: si falta una propiedad obligatoria o su valor es
 * inválido, se lanza {@link ConfigException} indicando qué propiedad corregir.
 */
public final class Config {

    private static final String RESOURCE = "application.properties";
    private static final String URL_KEY = "ollama.url";
    private static final String MODEL_KEY = "ollama.model";
    private static final String TIMEOUT_KEY = "ollama.timeout.seconds";

    /** Tiempo máximo para establecer la conexión TCP con Ollama. */
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(10);

    /** Timeout de request por defecto cuando la propiedad no está definida. */
    private static final Duration DEFAULT_REQUEST_TIMEOUT = Duration.ofSeconds(120);

    private final String url;
    private final String model;
    private final Duration requestTimeout;

    private Config(String url, String model, Duration requestTimeout) {
        this.url = url;
        this.model = model;
        this.requestTimeout = requestTimeout;
    }

    /**
     * Carga la configuración desde {@code application.properties} en el classpath.
     *
     * @throws ConfigException si el archivo no existe o su contenido no es válido
     */
    public static Config load() {
        InputStream input = Config.class.getClassLoader().getResourceAsStream(RESOURCE);
        if (input == null) {
            throw new ConfigException(
                    "No se encontró " + RESOURCE + " en el classpath; revisá src/main/resources");
        }
        try (input) {
            return load(input);
        } catch (IOException e) {
            throw new ConfigException("No se pudo leer " + RESOURCE, e);
        }
    }

    /**
     * Carga y valida la configuración desde un stream arbitrario.
     *
     * <p>Recibe el stream como parámetro para poder validar la configuración en tests
     * sin depender del classpath.
     */
    public static Config load(InputStream input) {
        if (input == null) {
            throw new ConfigException("No se recibió contenido de configuración");
        }

        Properties properties = new Properties();
        try {
            properties.load(input);
        } catch (IOException e) {
            throw new ConfigException("No se pudo leer la configuración", e);
        }

        String url = requireText(properties, URL_KEY);
        validateUrl(url);
        String model = requireText(properties, MODEL_KEY);

        return new Config(url, model, parseTimeout(properties));
    }

    /** URL del endpoint de generación de Ollama. */
    public String getUrl() {
        return url;
    }

    /** Modelo de Ollama a consultar. */
    public String getModel() {
        return model;
    }

    /** Tiempo máximo de espera de una respuesta completa. */
    public Duration getRequestTimeout() {
        return requestTimeout;
    }

    /** Tiempo máximo para establecer la conexión. */
    public Duration getConnectTimeout() {
        return CONNECT_TIMEOUT;
    }

    private static String requireText(Properties properties, String key) {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new ConfigException(
                    "Falta la propiedad obligatoria '" + key + "' en " + RESOURCE);
        }
        return value.trim();
    }

    private static void validateUrl(String url) {
        final URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new ConfigException("'" + URL_KEY + "' no es una URL válida: " + url, e);
        }

        boolean httpScheme = "http".equalsIgnoreCase(uri.getScheme())
                || "https".equalsIgnoreCase(uri.getScheme());
        if (!httpScheme || uri.getHost() == null) {
            throw new ConfigException(
                    "'" + URL_KEY + "' debe ser una URL http o https con host, por ejemplo "
                            + "http://localhost:11434/api/generate. Valor recibido: " + url);
        }
    }

    private static Duration parseTimeout(Properties properties) {
        String raw = properties.getProperty(TIMEOUT_KEY);
        if (raw == null || raw.isBlank()) {
            return DEFAULT_REQUEST_TIMEOUT;
        }
        try {
            long seconds = Long.parseLong(raw.trim());
            if (seconds <= 0) {
                throw new NumberFormatException("debe ser positivo");
            }
            return Duration.ofSeconds(seconds);
        } catch (NumberFormatException e) {
            throw new ConfigException(
                    "'" + TIMEOUT_KEY + "' debe ser un número entero positivo de segundos. "
                            + "Valor recibido: " + raw, e);
        }
    }
}

package org.example.config;

/**
 * Configuración inválida o ausente.
 *
 * <p>Se lanza al cargar {@code application.properties} para que el problema se vea
 * al arrancar y no como un error críptico en medio de una llamada al LLM.
 */
public class ConfigException extends RuntimeException {

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }
}

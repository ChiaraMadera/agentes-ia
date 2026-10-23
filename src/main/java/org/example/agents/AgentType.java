package org.example.agents;

import org.example.agents.desarrollo.GeneratorCodeAgent;
import org.example.agents.texto.TranslatorTextAgent;
import org.example.client.LlmClient;

import java.util.function.Function;

/**
 * Registro único de los agentes disponibles.
 *
 * <p>Concentra nombre visible, etiqueta de entrada y forma de creación. Agregar un
 * agente nuevo es agregar un valor acá: la UI arma el combo con {@link #values()} y
 * no necesita conocer las clases concretas.
 *
 * <p>Este enum reemplaza el mapeo posicional que antes vivía duplicado en la UI, en
 * la consola y en la fábrica.
 */
public enum AgentType {

    CODE_GENERATOR(
            "Generador de código",
            "Ingrese las especificaciones del código:",
            GeneratorCodeAgent::new),

    TEXT_TRANSLATOR(
            "Traductor de textos al español",
            "Ingrese el texto a traducir:",
            TranslatorTextAgent::new);

    private final String displayName;
    private final String inputLabel;
    private final Function<LlmClient, IAgent> factory;

    AgentType(String displayName, String inputLabel, Function<LlmClient, IAgent> factory) {
        this.displayName = displayName;
        this.inputLabel = inputLabel;
        this.factory = factory;
    }

    /**
     * Crea una instancia del agente con el cliente indicado.
     *
     * @param client cliente del LLM ya configurado
     */
    public IAgent create(LlmClient client) {
        return factory.apply(client);
    }

    /** Texto que la UI muestra como ayuda del campo de entrada. */
    public String getInputLabel() {
        return inputLabel;
    }

    /** {@code JComboBox} usa {@code toString()} para renderizar cada opción. */
    @Override
    public String toString() {
        return displayName;
    }
}

package org.example;

import org.example.client.LlmClient;
import org.example.client.OllamaClient;
import org.example.config.Config;
import org.example.config.ConfigException;
import org.example.ui.AgentUI;
import org.example.ui.Console;

import javax.swing.*;

/**
 * Punto de entrada y raíz de composición: arma la configuración y el cliente una sola
 * vez y los inyecta en la interfaz elegida.
 *
 * <p>Uso: sin argumentos abre la ventana; con {@code --console} usa la interfaz de texto.
 */
public class Main {

    private static final String CONSOLE_FLAG = "--console";

    public static void main(String[] args) {
        final Config config;
        try {
            config = Config.load();
        } catch (ConfigException e) {
            System.err.println("Configuración inválida: " + e.getMessage());
            System.exit(1);
            return;
        }

        LlmClient client = new OllamaClient(config);

        if (args.length > 0 && CONSOLE_FLAG.equalsIgnoreCase(args[0])) {
            new Console(client).menu();
            return;
        }

        SwingUtilities.invokeLater(() -> new AgentUI(client).setVisible(true));
    }
}

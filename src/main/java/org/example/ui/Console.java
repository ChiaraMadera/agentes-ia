package org.example.ui;

import org.example.agents.AgentType;
import org.example.client.LlmClient;
import org.example.client.LlmClientException;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Objects;
import java.util.Scanner;

/**
 * Alternativa por línea de comandos a {@link AgentUI}.
 *
 * <p>Usa el mismo registro {@link AgentType} que la interfaz gráfica. Lee siempre
 * líneas completas y parsea el número, de modo que una entrada no numérica produce
 * un mensaje de error en lugar de una excepción.
 */
public class Console {

    private final LlmClient client;
    private final Scanner scanner;
    private final PrintStream out;

    public Console(LlmClient client) {
        this(client, System.in, System.out);
    }

    /** Constructor que permite inyectar entrada y salida para pruebas. */
    public Console(LlmClient client, InputStream in, PrintStream out) {
        this.client = Objects.requireNonNull(client, "client");
        this.scanner = new Scanner(Objects.requireNonNull(in, "in"));
        this.out = Objects.requireNonNull(out, "out");
    }

    public void menu() {
        AgentType[] agents = AgentType.values();

        while (true) {
            printMenu(agents);

            String line = readLine();
            if (line == null) {
                return;
            }
            if (line.isEmpty()) {
                continue;
            }

            int option;
            try {
                option = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                out.println("Error: debe ingresar un número válido.");
                continue;
            }

            if (option == 0) {
                out.println("Saliendo.....");
                return;
            }
            if (option < 1 || option > agents.length) {
                out.println("Opción inválida, vuelva a intentar.");
                continue;
            }

            AgentType selected = agents[option - 1];
            out.println(selected.getInputLabel());

            String prompt = readLine();
            if (prompt == null) {
                return;
            }
            if (prompt.isEmpty()) {
                out.println("La entrada no puede estar vacía.");
                continue;
            }

            try {
                out.println(selected.create(client).execute(prompt));
            } catch (LlmClientException e) {
                out.println("Error: " + e.getMessage());
            }
        }
    }

    private void printMenu(AgentType[] agents) {
        out.println("Seleccione una opción:");
        for (int i = 0; i < agents.length; i++) {
            out.println((i + 1) + "- " + agents[i]);
        }
        out.println("0- Salir");
    }

    /** Devuelve la línea sin espacios, o {@code null} si la entrada terminó. */
    private String readLine() {
        if (!scanner.hasNextLine()) {
            return null;
        }
        return scanner.nextLine().trim();
    }
}

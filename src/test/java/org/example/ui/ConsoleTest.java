package org.example.ui;

import org.example.agents.AgentType;
import org.example.client.LlmClient;
import org.example.client.LlmClientException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleTest {

    private static String run(String input, LlmClient client) {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new Console(client, in, new PrintStream(out)).menu();
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void opcionInvalidaMuestraError() {
        LlmClient client = prompt -> "ok";
        String output = run("99\n0\n", client);
        assertTrue(output.contains("Opción inválida"));
    }

    @Test
    void entradaNoNumericaMuestraError() {
        LlmClient client = prompt -> "ok";
        String output = run("abc\n0\n", client);
        assertTrue(output.contains("debe ingresar un número válido"));
    }

    @Test
    void entradaVaciaMuestraError() {
        LlmClient client = prompt -> "ok";
        // option 1 then empty line
        String output = run("1\n\n0\n", client);
        assertTrue(output.contains("La entrada no puede estar vacía"));
    }

    @Test
    void ejecutaAgenteYmuestraResultado() {
        LlmClient client = prompt -> "respuesta del modelo";
        String output = run("1\nhola\n0\n", client);
        assertTrue(output.contains("respuesta del modelo"));
    }

    @Test
    void fallaDelClienteSeMuestra() {
        LlmClient client = prompt -> { throw new LlmClientException("boom"); };
        String output = run("1\nhola\n0\n", client);
        assertTrue(output.contains("Error: boom"));
    }

    @Test
    void salirTermina() {
        LlmClient client = prompt -> "ok";
        String output = run("0\n", client);
        assertTrue(output.toLowerCase().contains("saliendo"));
    }
}
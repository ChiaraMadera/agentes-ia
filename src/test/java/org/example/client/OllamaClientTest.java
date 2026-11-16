package org.example.client;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.example.config.Config;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OllamaClientTest {

    private HttpServer server;

    private Config configFor(int port) throws Exception {
        String props = "ollama.url=http://localhost:" + port + "/api/generate\n"
                + "ollama.model=llama3.2:3b\n"
                + "ollama.timeout.seconds=5\n";
        return Config.load(new java.io.ByteArrayInputStream(props.getBytes(StandardCharsets.UTF_8)));
    }

    private HttpServer startServer(com.sun.net.httpserver.HttpHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/api/generate", handler);
        server.start();
        return server;
    }

    @AfterEach
    void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void statusNo2xxLanzaExcepcionConDetalle() throws Exception {
        startServer(exchange -> {
            byte[] body = "{\"error\":\"model not found\"}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(404, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        Config config = configFor(server.getAddress().getPort());
        OllamaClient client = new OllamaClient(config);

        LlmClientException ex = assertThrows(LlmClientException.class,
                () -> client.generate("hola"));
        assertTrue(ex.getMessage().contains("404"));
        assertTrue(ex.getMessage().contains("model not found"));
    }

    @Test
    void respuestaVaciaLanzaExcepcion() throws Exception {
        startServer(exchange -> {
            byte[] body = "{\"response\":\"   \"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        Config config = configFor(server.getAddress().getPort());
        OllamaClient client = new OllamaClient(config);

        assertThrows(LlmClientException.class, () -> client.generate("hola"));
    }

    @Test
    void bodyNoJsonLanzaExcepcion() throws Exception {
        startServer(exchange -> {
            byte[] body = "<html>proxy error</html>".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        Config config = configFor(server.getAddress().getPort());
        OllamaClient client = new OllamaClient(config);

        LlmClientException ex = assertThrows(LlmClientException.class,
                () -> client.generate("hola"));
        assertTrue(ex.getMessage().contains("JSON"));
    }

    @Test
    void respuestaExitosaDevuelveTexto() throws Exception {
        startServer(exchange -> {
            byte[] body = "{\"response\":\"Hola mundo\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        Config config = configFor(server.getAddress().getPort());
        OllamaClient client = new OllamaClient(config);

        assertEquals("Hola mundo", client.generate("hola"));
    }

    @Test
    void promptVacioLanzaExcepcion() throws Exception {
        Config config = configFor(1234);
        OllamaClient client = new OllamaClient(config);
        assertThrows(LlmClientException.class, () -> client.generate("   "));
    }

    @Test
    void timeoutLanzaExcepcion() throws Exception {
        startServer(exchange -> {
            try {
                Thread.sleep(10_000);
            } catch (InterruptedException ignored) {
            }
            byte[] body = "{\"response\":\"tarde\"}".getBytes(StandardCharsets.UTF_8);
            try {
                exchange.sendResponseHeaders(200, body.length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(body);
                }
            } catch (IOException ignored) {
            }
        });

        String props = "ollama.url=http://localhost:" + server.getAddress().getPort() + "/api/generate\n"
                + "ollama.model=llama3.2:3b\n"
                + "ollama.timeout.seconds=1\n";
        Config config = Config.load(new java.io.ByteArrayInputStream(props.getBytes(StandardCharsets.UTF_8)));
        OllamaClient client = new OllamaClient(config);

        LlmClientException ex = assertThrows(LlmClientException.class,
                () -> client.generate("hola"));
        assertTrue(ex.getMessage().toLowerCase().contains("no respondió")
                || ex.getMessage().toLowerCase().contains("timeout"));
    }
}
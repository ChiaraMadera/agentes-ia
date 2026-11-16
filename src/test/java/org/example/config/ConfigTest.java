package org.example.config;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigTest {

    private static InputStream stream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    @Test
    void cargaConfiguracionValida() {
        String props = """
                ollama.url=http://localhost:11434/api/generate
                ollama.model=llama3.2:3b
                ollama.timeout.seconds=60
                """;

        Config config = Config.load(stream(props));

        assertEquals("http://localhost:11434/api/generate", config.getUrl());
        assertEquals("llama3.2:3b", config.getModel());
        assertEquals(60, config.getRequestTimeout().toSeconds());
        assertEquals(10, config.getConnectTimeout().toSeconds());
    }

    @Test
    void timeoutPorDefectoCuandoNoSeDefine() {
        String props = """
                ollama.url=http://localhost:11434/api/generate
                ollama.model=llama3.2:3b
                """;

        Config config = Config.load(stream(props));

        assertEquals(120, config.getRequestTimeout().toSeconds());
    }

    @Test
    void fallaSiFaltaPropiedadObligatoria() {
        String props = """
                ollama.url=http://localhost:11434/api/generate
                """;

        ConfigException ex = assertThrows(ConfigException.class, () -> Config.load(stream(props)));
        assertTrue(ex.getMessage().contains("ollama.model"));
    }

    @Test
    void fallaSiUrlNoEsHttp() {
        String props = """
                ollama.url=ftp://localhost:11434/api/generate
                ollama.model=llama3.2:3b
                """;

        ConfigException ex = assertThrows(ConfigException.class, () -> Config.load(stream(props)));
        assertTrue(ex.getMessage().contains("ollama.url"));
    }

    @Test
    void fallaSiTimeoutNoEsPositivo() {
        String props = """
                ollama.url=http://localhost:11434/api/generate
                ollama.model=llama3.2:3b
                ollama.timeout.seconds=0
                """;

        assertThrows(ConfigException.class, () -> Config.load(stream(props)));
    }

    @Test
    void fallaSiStreamEsNull() {
        assertThrows(ConfigException.class, () -> Config.load(null));
    }
}
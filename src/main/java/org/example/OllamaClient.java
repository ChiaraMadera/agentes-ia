package org.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

public class OllamaClient {
    // URL del endpoint de generacion de Ollama
    private static final String OLLAMA_URL = "http://localhost:11434/api/generate";

    // Cliente HTTP
    private static final HttpClient client = HttpClient.newHttpClient();

    // Mapper de Jackson para JSON
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String generate(String prompt){
        try{
            // 1. Cuerpo de la peticion
            Map<String, Object> payload = new HashMap<>();
            payload.put("model", "llama3.2:1b");
            payload.put("prompt", prompt);
            payload.put("stream", false);

            // 2. Convertir map a JSON
            String json = mapper.writeValueAsString(payload);

            // 3. Construir peticion Http tipo POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            // 4. Enviar la peticion y recibir la respuesta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 5 . Verificar que la respuesta sea exitosa
            if(response.statusCode() != 200){
                return "Error: " + response.statusCode() +": " + response.body();

            }
            // 6. Parsear el JSON de la respuesta
            JsonNode root = mapper.readTree(response.body());

            // 7. Extraemos el texto generado
            return  root.path("response").asText();

        }catch(Exception e){
            return "Error: " + e.getMessage();

        }
    }


}

package org.example.client;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.config.Config;
import org.example.dtos.RequestDTO;
import org.example.dtos.ResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class LLMClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(LLMClient.class);


    // Configuracion del LLM cargada desde application.propierties
    private static final Config config = new Config();

    // Cliente HTTP
    private static final HttpClient client = HttpClient.newHttpClient();

    // Mapper de Jackson para JSON
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String generate(String prompt){
        try{
            /**
             * Logger para registro de eventos y errores
             */
            LOGGER.info("Enviando prompt al LLM: {}", config.getModel());

            // 1. Cuerpo de la petición
            RequestDTO requestBody =
                    new RequestDTO(
                            config.getModel(),
                            prompt,
                            false
                    );

            // 2. Serializar peticion a JSON
            String json = mapper.writeValueAsString(requestBody);

            // 3. Construir peticion Http tipo POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.getUrl()))
                    .header("Content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();
            // 4. Enviar la peticion y recibir la respuesta
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 5 . Verificar que la respuesta sea exitosa
            LOGGER.info("Respuesta recibida, Status: {}", response.statusCode());



            // 6. Deserializar la respuesta de JSON a DTO
            ResponseDTO result = mapper.readValue(response.body(), ResponseDTO.class);

            // Validar que la respuesta no sea nula o vacia
            if (result == null
                    || result.response() == null
                    || result.response().isBlank()) {

                LOGGER.error("El LLM devolvió una respuesta vacía");

                throw new RuntimeException(
                        "Respuesta vacía recibida desde el LLM"
                );
            }


            // 7. Extraemos el texto generado
            return  result.response();

        }catch(Exception e){
            return "Error: " + e.getMessage();

        }
    }


}

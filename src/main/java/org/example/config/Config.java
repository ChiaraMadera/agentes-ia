package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Gestor de configuracion para la conexion con el LLM.
 * Cargar propiedades desde el archivp application.properties.
 */
public class Config {
    /**
     * Almacen de propiedades cargadas desde el archivo de configuracion
     */
    private final Properties properties;

    /**
     * Crear el constructor que carga la configuracion desde application.propierties
     */
    public Config(){
        properties = new Properties();

        try (InputStream input =
                getClass()
                        .getClassLoader()
                        .getResourceAsStream("application.properties")
                ){
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error cargando configuracion",e);
        }

    }
    /**
     * Obtiene la URL del endpoint del LLM
     */
    public String getUrl(){
        return properties.getProperty("ollama.url");
    }
    /**
     * Obtiene el modelo de IA a utilizar
     */
    public String getModel(){
        return properties.getProperty("ollama.model");
    }

}

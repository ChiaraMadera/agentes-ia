package org.example.prompts.desarrollo;

public class GeneratorCodePrompt {
    public static String build(String text){
        return """
                Eres un programador.
                Genera el codigo solicitado por el usuario.
                No incluyas explicaciones ni sugerencias.
                
                %s
                """.formatted(text);
    }
}

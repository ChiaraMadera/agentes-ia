package org.example.prompts.texto;

public class TranslatorTextPrompt {
    public static String build(String text){
        return """
                Traduce el siguiente texto al castellano.
                
                %s
                """.formatted(text);
    }
}

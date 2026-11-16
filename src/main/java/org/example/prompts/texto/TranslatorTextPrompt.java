package org.example.prompts.texto;

public final class TranslatorTextPrompt {
    private TranslatorTextPrompt() {
    }

    public static String build(String text){
        return """
                Traduce el siguiente texto al castellano.
                
                %s
                """.formatted(text);
    }
}

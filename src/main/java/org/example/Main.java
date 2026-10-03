package org.example;


import static org.example.OllamaClient.generate;

public class Main {
    public static void main(String[] args) {
        // Prompt de ejemplo
        String testPrompt = """
                
                Traduci al español el siguiente texto: 
                The little brown dog runs fast through green grass every sunny morning. 
                He chases colorful butterflies near tall trees and drinks fresh water from a small blue bowl. 
                After a long and happy day of joyful play, 
                the tired puppy sleeps quietly beside his warm bed until tomorrow comes again.

                """;
        // Enviar el prompt a Ollama y mostrar la respuesta.
        System.out.println(generate(testPrompt));

    }

}



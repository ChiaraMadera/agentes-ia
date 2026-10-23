package org.example;

import org.example.ui.AgentUI;

public class Main {
    public static void main(String[] args) throws Exception {
        //Console console = new Console();
        //console.menu();

        javax.swing.SwingUtilities.invokeLater(AgentUI::new);


    }
}

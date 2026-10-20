package org.example.ui;

import org.example.agents.IAgent;
import org.example.factory.AgentFactory;

import javax.swing.*;
import java.awt.*;

public class AgentUI extends JFrame {

    // Combo box
    private static final String[] AGENTES = {
            "Generador de código",
            "Traductor de textos al español"
    };

    // Combo box
    private static final String[] LABELS_INPUT = {
            "Ingrese las especificaciones del codigo:",
            "Ingrese el texto a traducir :"
    };

    private final JComboBox<String> agentComboBox;
    private final JLabel inputLabel;
    private final JTextArea inputArea;
    private final JTextArea outputArea;
    private final JButton executeButton;
    private final JButton clearButton;
    private final JProgressBar progressBar;

    public AgentUI() {
        setTitle("Agentes IA - Agentes Inteligentes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // Panel superior: selección de agente
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.add(new JLabel("Seleccione el agente:"));
        agentComboBox = new JComboBox<>(AGENTES);
        agentComboBox.addActionListener(e -> updateInputLabel());
        topPanel.add(agentComboBox);
        add(topPanel, BorderLayout.NORTH);

        // Panel central: input y output
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 10, 5, 10);

        // Label de input
        inputLabel = new JLabel(LABELS_INPUT[0]);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 0.0;
        centerPanel.add(inputLabel, gbc);

        // Área de input
        inputArea = new JTextArea(5, 40);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(BorderFactory.createTitledBorder("Entrada"));
        gbc.gridy = 1;
        gbc.weighty = 0.4;
        centerPanel.add(inputScroll, gbc);

        // Barra de progreso (oculta por defecto)
        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setString("Procesando...");
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        gbc.gridy = 2;
        gbc.weighty = 0.0;
        centerPanel.add(progressBar, gbc);

        // Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        executeButton = new JButton("Ejecutar");
        executeButton.addActionListener(e -> executeAgent());
        clearButton = new JButton("Limpiar");
        clearButton.addActionListener(e -> clearFields());
        buttonPanel.add(executeButton);
        buttonPanel.add(clearButton);
        gbc.gridy = 3;
        gbc.weighty = 0.0;
        centerPanel.add(buttonPanel, gbc);

        // Área de output
        outputArea = new JTextArea(8, 40);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setEditable(false);
        outputArea.setBackground(new Color(245, 245, 245));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Resultado"));
        gbc.gridy = 4;
        gbc.weighty = 0.6;
        centerPanel.add(outputScroll, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Panel inferior: autoría
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        JLabel authorLabel = new JLabel("Desarrollado por Chiara Madera");
        authorLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        authorLabel.setForeground(Color.GRAY);
        bottomPanel.add(authorLabel);
        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void updateInputLabel() {
        int index = agentComboBox.getSelectedIndex();
        if (index >= 0 && index < LABELS_INPUT.length) {
            inputLabel.setText(LABELS_INPUT[index]);
        }
        inputArea.requestFocusInWindow();
    }

    private void executeAgent() {
        int index = agentComboBox.getSelectedIndex();
        if (index < 0) {
            JOptionPane.showMessageDialog(this,
                    "Seleccione un agente.",
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String input = inputArea.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "El campo de entrada no puede estar vacío.",
                    "Error",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        executeButton.setEnabled(false);
        agentComboBox.setEnabled(false);
        progressBar.setVisible(true);
        outputArea.setText("");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                int option = index + 1;
                IAgent agent = AgentFactory.create(option);
                return agent.execute(input);
            }

            @Override
            protected void done() {
                try {
                    String result = get();
                    outputArea.setText(result);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AgentUI.this,
                            "Error al ejecutar el agente: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    executeButton.setEnabled(true);
                    agentComboBox.setEnabled(true);
                    progressBar.setVisible(false);
                }
            }
        };

        worker.execute();
    }

    private void clearFields() {
        inputArea.setText("");
        outputArea.setText("");
        progressBar.setVisible(false);
        inputArea.requestFocusInWindow();
    }
}

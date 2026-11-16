package org.example.ui;

import org.example.agents.AgentType;
import org.example.client.LlmClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * Ventana principal: selección de agente, entrada, resultado y estado de progreso.
 *
 * <p>El cliente del LLM se recibe por constructor. La ventana no arma sus propias
 * dependencias ni se muestra a sí misma: eso queda a cargo del punto de entrada.
 */
public class AgentUI extends JFrame {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentUI.class);

    private final LlmClient client;

    private final JComboBox<AgentType> agentComboBox;
    private final JLabel inputLabel;
    private final JTextArea inputArea;
    private final JTextArea outputArea;
    private final JButton executeButton;
    private final JButton cancelButton;
    private final JButton clearButton;
    private final JProgressBar progressBar;

    private SwingWorker<String, Void> currentWorker;

    public AgentUI(LlmClient client) {
        this.client = Objects.requireNonNull(client, "client");

        setTitle("Agentes IA - Agentes Inteligentes");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        ((JPanel) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel superior: selección de agente
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.add(new JLabel("Seleccione el agente:"));
        agentComboBox = new JComboBox<>(AgentType.values());
        agentComboBox.addActionListener(e -> updateInputLabel());
        topPanel.add(agentComboBox);
        add(topPanel, BorderLayout.NORTH);

        // Panel central: input y output
        JPanel centerPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(5, 10, 5, 10);

        // Label de input
        inputLabel = new JLabel(((AgentType) agentComboBox.getSelectedItem()).getInputLabel());
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

        // Área de output
        outputArea = new JTextArea(8, 40);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Resultado"));

        // Split redimensionable entre entrada y resultado
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, inputScroll, outputScroll);
        splitPane.setResizeWeight(0.4);
        splitPane.setOneTouchExpandable(true);
        gbc.gridy = 1;
        gbc.weighty = 1.0;
        centerPanel.add(splitPane, gbc);

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
        executeButton.setMnemonic('E');
        executeButton.addActionListener(e -> executeAgent());
        cancelButton = new JButton("Cancelar");
        cancelButton.setMnemonic('C');
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(e -> cancelExecution());
        clearButton = new JButton("Limpiar");
        clearButton.setMnemonic('L');
        clearButton.addActionListener(e -> clearFields());
        buttonPanel.add(executeButton);
        buttonPanel.add(cancelButton);
        buttonPanel.add(clearButton);
        gbc.gridy = 3;
        gbc.weighty = 0.0;
        centerPanel.add(buttonPanel, gbc);

        add(centerPanel, BorderLayout.CENTER);

        // Botón por defecto + atajo Ctrl+Enter para ejecutar
        getRootPane().setDefaultButton(executeButton);
        InputMap inputMap = inputArea.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = inputArea.getActionMap();
        inputMap.put(KeyStroke.getKeyStroke("control ENTER"), "execute");
        actionMap.put("execute", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                executeAgent();
            }
        });

        // Panel inferior: autoría
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
        JLabel authorLabel = new JLabel("Desarrollado por Chiara Madera");
        authorLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        authorLabel.setForeground(Color.GRAY);
        bottomPanel.add(authorLabel);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void updateInputLabel() {
        AgentType selected = (AgentType) agentComboBox.getSelectedItem();
        if (selected != null) {
            inputLabel.setText(selected.getInputLabel());
        }
        inputArea.requestFocusInWindow();
    }

    private void executeAgent() {
        AgentType selected = (AgentType) agentComboBox.getSelectedItem();
        if (selected == null) {
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
        cancelButton.setEnabled(true);
        agentComboBox.setEnabled(false);
        progressBar.setVisible(true);
        outputArea.setText("");

        currentWorker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                selected.create(client).executeStreaming(input, chunk ->
                        SwingUtilities.invokeLater(() -> outputArea.append(chunk)));
                return null;
            }

            @Override
            protected void done() {
                try {
                    if (isCancelled()) {
                        outputArea.setText("Operación cancelada.");
                        return;
                    }
                    String result = get();
                    if (result != null && !result.isEmpty() && outputArea.getText().isEmpty()) {
                        outputArea.setText(result);
                    }
                } catch (java.util.concurrent.CancellationException e) {
                    outputArea.setText("Operación cancelada.");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    showExecutionError(e);
                } finally {
                    executeButton.setEnabled(true);
                    cancelButton.setEnabled(false);
                    agentComboBox.setEnabled(true);
                    progressBar.setVisible(false);
                    currentWorker = null;
                }
            }
        };

        currentWorker.execute();
    }

    private void cancelExecution() {
        if (currentWorker != null && !currentWorker.isDone()) {
            currentWorker.cancel(true);
        }
    }

    /**
     * Muestra la causa real del fallo en vez del envoltorio {@link ExecutionException}
     * que agrega {@link SwingWorker#get()}.
     */
    private void showExecutionError(ExecutionException e) {
        Throwable cause = e.getCause() != null ? e.getCause() : e;
        String message = cause.getMessage() != null ? cause.getMessage() : cause.toString();

        LOGGER.error("Error al ejecutar el agente", cause);
        outputArea.setText("Error: " + message);
        JOptionPane.showMessageDialog(this,
                message,
                "Error al ejecutar el agente",
                JOptionPane.ERROR_MESSAGE);
    }

    private void clearFields() {
        inputArea.setText("");
        outputArea.setText("");
        progressBar.setVisible(false);
        inputArea.requestFocusInWindow();
    }
}

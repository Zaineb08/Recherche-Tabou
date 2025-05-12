package gui;

import algo.TabuSearchContinuous;
import java.awt.*;
import java.util.List;
import javax.swing.*;

public class ContinuousPanel extends JPanel {
    private JTextArea outputArea;

    public ContinuousPanel() {
        setLayout(new BorderLayout(10,10));

        // ==== Paramètres ====
        JPanel paramPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        paramPanel.setBorder(BorderFactory.createTitledBorder("Paramètres"));
        paramPanel.add(new JLabel("Début de l'intervalle:"));
        JTextField startField = new JTextField("-2");
        paramPanel.add(startField);

        paramPanel.add(new JLabel("Fin de l'intervalle:"));
        JTextField endField = new JTextField("3");
        paramPanel.add(endField);

        paramPanel.add(new JLabel("Itérations:"));
        JTextField iterField = new JTextField("100");
        paramPanel.add(iterField);

        paramPanel.add(new JLabel("Taille Tabu:"));
        JTextField tabuField = new JTextField("10");
        paramPanel.add(tabuField);

        add(paramPanel, BorderLayout.NORTH);

        // ==== Zone de sortie ====
        outputArea = new JTextArea();
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        outputArea.setEditable(false);
        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Sortie"));
        add(scroll, BorderLayout.CENTER);

        // ==== Bouton Exécuter ====
        JButton runButton = new JButton("Exécuter Tabu Search");
        runButton.addActionListener(e -> {
            try {
                double start      = Double.parseDouble(startField.getText());
                double end        = Double.parseDouble(endField.getText());
                int    iterations = Integer.parseInt(iterField.getText());
                int    tabuSize   = Integer.parseInt(tabuField.getText());

                outputArea.setText("");
                runButton.setEnabled(false);

                SwingWorker<Double, String> worker = new SwingWorker<>() {
                    @Override
                    protected Double doInBackground() {
                        TabuSearchContinuous ts =
                          new TabuSearchContinuous(iterations, tabuSize);
                        return ts.optimize(start, end, (it, msg) -> {
                            // TabuSearchContinuous only calls back on its key iterations
                            publish(msg + "\n");
                        });
                    }

                    @Override
                    protected void process(List<String> chunks) {
                        chunks.forEach(outputArea::append);
                    }

                    @Override
                    protected void done() {
                        // ← only re-enable button; final output comes from the listener
                        runButton.setEnabled(true);
                    }
                };
                worker.execute();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                  this,
                  "Veuillez entrer des nombres valides pour tous les champs.",
                  "Erreur de saisie",
                  JOptionPane.ERROR_MESSAGE
                );
            }
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.add(runButton);
        add(south, BorderLayout.SOUTH);
    }
}

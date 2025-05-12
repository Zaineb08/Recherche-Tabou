package gui;

import algo.TabuSearchKnapsack;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import model.Item;
import utils.CSVUtils;

/**
 * Panneau Amélioré pour le problème du Sac à Dos avec Recherche Tabou.
 * Utilise GridBagLayout et une police plus grande pour la zone de sortie.
 */
public class KnapsackPanel extends JPanel {
    private JTextArea outputArea;
    private JButton runButton;
    private List<Item> items = new ArrayList<>();

    public KnapsackPanel() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ========== Panneau de paramètres ==========
        JPanel paramPanel = new JPanel(new GridBagLayout());
        paramPanel.setBorder(BorderFactory.createTitledBorder("Paramètres"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(5, 5, 5, 5);
        gbc.anchor  = GridBagConstraints.EAST;

        // Charger CSV
        gbc.gridx = 0; gbc.gridy = 0;
        paramPanel.add(new JLabel("Fichier CSV:"), gbc);
        gbc.gridx = 1;
        JButton loadCSVButton = new JButton("Charger CSV");
        paramPanel.add(loadCSVButton, gbc);

        // Capacité
        gbc.gridx = 0; gbc.gridy = 1;
        paramPanel.add(new JLabel("Capacité:"), gbc);
        gbc.gridx = 1;
        JTextField capField = new JTextField("50.0", 8);
        paramPanel.add(capField, gbc);

        // Itérations
        gbc.gridx = 0; gbc.gridy = 2;
        paramPanel.add(new JLabel("Itérations:"), gbc);
        gbc.gridx = 1;
        JTextField iterField = new JTextField("100", 8);
        paramPanel.add(iterField, gbc);

        // Taille Tabu
        gbc.gridx = 0; gbc.gridy = 3;
        paramPanel.add(new JLabel("Taille Tabu:"), gbc);
        gbc.gridx = 1;
        JTextField tabuField = new JTextField("10", 8);
        paramPanel.add(tabuField, gbc);

        add(paramPanel, BorderLayout.NORTH);

        // ========== Zone de sortie ==========
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        outputArea.setBorder(BorderFactory.createTitledBorder("Sortie"));
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        add(scrollPane, BorderLayout.CENTER);

        // ========== Bouton Exécuter ==========
        runButton = new JButton("Exécuter Tabu Search");
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.add(runButton);
        add(southPanel, BorderLayout.SOUTH);

        // ========== Actions ==========
        loadCSVButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    List<String[]> data = CSVUtils.readCSV(file);
                    items.clear();
                    for (String[] line : data) {
                        if (line.length >= 3) {
                            try {
                                int id    = Integer.parseInt(line[0].trim());
                                double w  = Double.parseDouble(line[1].trim());
                                double v  = Double.parseDouble(line[2].trim());
                                items.add(new Item(id, w, v));
                            } catch (NumberFormatException ignored) { }
                        }
                    }
                    outputArea.append(
                      String.format("CSV chargé: %d items.%n", items.size())
                    );
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(
                      this,
                      "Erreur chargement CSV: " + ex.getMessage(),
                      "Erreur",
                      JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        });

        runButton.addActionListener(e -> {
            if (items.isEmpty()) {
                JOptionPane.showMessageDialog(
                  this,
                  "Aucun item chargé. Veuillez importer un CSV valide.",
                  "Avertissement",
                  JOptionPane.WARNING_MESSAGE
                );
                return;
            }
            try {
                double cap = Double.parseDouble(capField.getText());
                int    iters = Integer.parseInt(iterField.getText());
                int    tabu  = Integer.parseInt(tabuField.getText());

                outputArea.setText("");
                runButton.setEnabled(false);

                SwingWorker<boolean[], String> worker = new SwingWorker<>() {
                    @Override
                    protected boolean[] doInBackground() {
                        TabuSearchKnapsack ts =
                          new TabuSearchKnapsack(iters, tabu, items, cap);
                        return ts.optimize((iteration, message) -> {
                            if (iteration == 1
                             || iteration == 2
                             || iteration == 10
                             || iteration == iters) {
                                publish(message + "\n");
                            }
                        });
                    }

                    @Override
                    protected void process(List<String> chunks) {
                        chunks.forEach(outputArea::append);
                    }

                    @Override
                    protected void done() {
                        // — removed duplicate final append —
                        runButton.setEnabled(true);
                    }
                };
                worker.execute();

            } catch (NumberFormatException nfe) {
                JOptionPane.showMessageDialog(
                  this,
                  "Veuillez saisir des nombres valides.",
                  "Erreur de saisie",
                  JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}

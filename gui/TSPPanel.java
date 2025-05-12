package gui;

import algo.TabuSearchTSP;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import javax.swing.*;
import model.City;
import utils.CSVUtils;

/**
 * Panneau de configuration et d'exécution de la Recherche Tabou pour le TSP.
 * Affiche les itérations clés et la meilleure route finale.
 */
public class TSPPanel extends JPanel {
    private final JTextArea outputArea;
    private final JButton loadCSVButton;
    private final JButton runButton;
    private final JTextField iterField;
    private final JTextField tabuField;
    private final List<City> cities = new ArrayList<>();

    public TSPPanel() {
        super(new BorderLayout(10, 10));

        // ===== Panneau des paramètres =====
        JPanel paramPanel = new JPanel(new GridBagLayout());
        paramPanel.setBorder(BorderFactory.createTitledBorder("Paramètres"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.EAST;

        // Charger CSV
        gbc.gridx = 0; gbc.gridy = 0;
        paramPanel.add(new JLabel("Fichier CSV:"), gbc);
        gbc.gridx = 1;
        loadCSVButton = new JButton("Charger CSV");
        paramPanel.add(loadCSVButton, gbc);

        // Itérations
        gbc.gridx = 0; gbc.gridy = 1;
        paramPanel.add(new JLabel("Itérations:"), gbc);
        gbc.gridx = 1;
        iterField = new JTextField("100", 8);
        paramPanel.add(iterField, gbc);

        // Taille Tabu
        gbc.gridx = 0; gbc.gridy = 2;
        paramPanel.add(new JLabel("Taille Tabu:"), gbc);
        gbc.gridx = 1;
        tabuField = new JTextField("10", 8);
        paramPanel.add(tabuField, gbc);

        add(paramPanel, BorderLayout.NORTH);

        // ===== Zone de sortie =====
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(outputArea);
        scroll.setBorder(BorderFactory.createTitledBorder("Sortie"));
        add(scroll, BorderLayout.CENTER);

        // ===== Boutons =====
        loadCSVButton.addActionListener(e -> onLoadCSV());
        runButton = new JButton("Exécuter Tabu Search TSP");
        runButton.addActionListener(e -> onRun());
        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.add(runButton);
        add(south, BorderLayout.SOUTH);
    }

    private void onLoadCSV() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                List<String[]> data = CSVUtils.readCSV(file);
                cities.clear();
                for (String[] line : data) {
                    if (line.length >= 3) {
                        int id = Integer.parseInt(line[0].trim());
                        double x = Double.parseDouble(line[1].trim());
                        double y = Double.parseDouble(line[2].trim());
                        cities.add(new City(id, x, y));
                    }
                }
                outputArea.append(
                    String.format("CSV chargé : %d villes.%n", cities.size())
                );
            } catch (IOException | NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Erreur chargement CSV : " + ex.getMessage(),
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void onRun() {
        if (cities.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "Aucune ville chargée. Importez un CSV valide.",
                "Avertissement",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
        try {
            final int iterations = Integer.parseInt(iterField.getText().trim());
            final int tabuSize   = Integer.parseInt(tabuField.getText().trim());

            outputArea.setText("");
            runButton.setEnabled(false);

            SwingWorker<List<City>, String> worker = new SwingWorker<>() {
                @Override
                protected List<City> doInBackground() {
                    TabuSearchTSP ts = new TabuSearchTSP(iterations, tabuSize, cities);
                    return ts.optimize((iter, msg) -> {
                        if (iter == 1 || iter == 2 || iter == 10 || iter == iterations) {
                            publish(msg + "\n");
                        }
                    });
                }

                @Override
                protected void process(List<String> chunks) {
                    chunks.forEach(outputArea::append);
                }

                @Override
                protected void done() {
                    try {
                        List<City> bestRoute = get();
                        String routeStr = new TabuSearchTSP(iterations, tabuSize, cities)
                            .routeToString(bestRoute);
                        outputArea.append(
                            "Meilleure route trouvée : " + routeStr + "\n"
                        );
                    } catch (InterruptedException | ExecutionException ex) {
                        JOptionPane.showMessageDialog(
                            TSPPanel.this,
                            "Erreur pendant l'exécution : " + ex.getMessage(),
                            "Erreur",
                            JOptionPane.ERROR_MESSAGE
                        );
                    } finally {
                        runButton.setEnabled(true);
                    }
                }
            };
            worker.execute();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                this,
                "Veuillez entrer des nombres valides pour itérations et taille Tabu.",
                "Erreur de saisie",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
}

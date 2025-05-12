package app;

import gui.ContinuousPanel;
import gui.KnapsackPanel;
import gui.TSPPanel;

import javax.swing.*;

public class MainFrame extends JFrame {
    public MainFrame() {
        setTitle("Projet Tabu Search");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Fonction continue", new ContinuousPanel());
        tabbedPane.addTab("Sac à dos", new KnapsackPanel());
        tabbedPane.addTab("Voyageur de commerce", new TSPPanel());

        add(tabbedPane);
        setVisible(true);
    }
}

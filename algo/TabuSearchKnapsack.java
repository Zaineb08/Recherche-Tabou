package algo;

import java.util.*;
import model.Item;
import model.IterationListener;  // includes List, LinkedList, ArrayList, Arrays, etc.

/**
 * Recherche Tabou pour le problème du sac à dos.
 * Ne notifie le listener qu'aux itérations 1, 2, 10 et maxIter.
 */
public class TabuSearchKnapsack {
    private final int maxIter;
    private final int tabuListSize;
    private final List<Item> items;
    private final double capacity;
    private final LinkedList<boolean[]> tabuList;

    public TabuSearchKnapsack(int maxIter, int tabuListSize, List<Item> items, double capacity) {
        this.maxIter      = maxIter;
        this.tabuListSize = tabuListSize;
        this.items        = items;
        this.capacity     = capacity;
        this.tabuList     = new LinkedList<>();
    }

    /** Calcule la valeur de la solution (ou -∞ si surcharge). */
    private double evaluate(boolean[] solution) {
        double w = 0, v = 0;
        for (int i = 0; i < solution.length; i++) {
            if (solution[i]) {
                w += items.get(i).weight;
                v += items.get(i).value;
            }
        }
        return (w > capacity) ? Double.NEGATIVE_INFINITY : v;
    }

    /** Calcule le poids total d'une solution. */
    private double weight(boolean[] sol) {
        double w = 0;
        for (int i = 0; i < sol.length; i++)
            if (sol[i])
                w += items.get(i).weight;
        return w;
    }

    /** Génère une solution aléatoire (0/1) */
    private boolean[] randomSolution() {
        boolean[] s = new boolean[items.size()];
        for (int i = 0; i < s.length; i++)
            s[i] = Math.random() < 0.5;
        return s;
    }

    /** Copie un vecteur binaire */
    private boolean[] copySolution(boolean[] sol) {
        return Arrays.copyOf(sol, sol.length);
    }

    /** Compare deux vecteurs binaires */
    private boolean solutionsEqual(boolean[] a, boolean[] b) {
        return Arrays.equals(a, b);
    }

    /**
     * Génère tous les voisins à distance de Hamming 1 ou 2, valides (<= capacity).
     */
    private List<boolean[]> generateNeighbors(boolean[] current) {
        List<boolean[]> neigh = new ArrayList<>();
        int n = current.length;

        // flips 1 bit
        for (int i = 0; i < n; i++) {
            boolean[] c = copySolution(current);
            c[i] = !c[i];
            if (weight(c) <= capacity) neigh.add(c);
        }

        // flips 2 bits
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                boolean[] c = copySolution(current);
                c[i] = !c[i];
                c[j] = !c[j];
                if (weight(c) <= capacity) neigh.add(c);
            }
        }

        return neigh;
    }

    /**
     * Lance la Recherche Tabou.
     *
     * @param listener reçoit les messages aux itérations clefs
     * @return la meilleure solution binaire trouvée
     */
    public boolean[] optimize(IterationListener listener) {
        // 1) initialisation : start with a feasible solution
        boolean[] bestSolution;
        do {
            bestSolution = randomSolution();
        } while (weight(bestSolution) > capacity);

        double bestValue = evaluate(bestSolution);
        boolean[] current = copySolution(bestSolution);

        // 2) boucle principale
        for (int iter = 1; iter <= maxIter; iter++) {
            List<boolean[]> neighbors = generateNeighbors(current);
            boolean[] bestNeighbor = null;
            double bestNeighborValue = Double.NEGATIVE_INFINITY;

            // 2.a) choisir le meilleur voisin admissible (aspiration incluse)
            for (boolean[] cand : neighbors) {
                double val = evaluate(cand);
                boolean isTabu = tabuList.stream().anyMatch(tsol -> solutionsEqual(cand, tsol));

                // aspiration : autoriser si meilleur que global best
                if (isTabu && val <= bestValue) continue;

                if (val > bestNeighborValue) {
                    bestNeighbor = cand;
                    bestNeighborValue = val;
                }
            }

            // 2.b) mise à jour global best
            if (bestNeighbor != null && bestNeighborValue > bestValue) {
                bestSolution = copySolution(bestNeighbor);
                bestValue = bestNeighborValue;
            }

            // 2.c) passage au voisin
            if (bestNeighbor != null) {
                current = bestNeighbor;
            }

            // 2.d) mise à jour de la liste Tabu
            tabuList.add(copySolution(current));
            if (tabuList.size() > tabuListSize) {
                tabuList.removeFirst();
            }

            // 3) notification aux itérations clefs
            if (iter == 1 || iter == 2 || iter == 10 || iter == maxIter) {
                double currentValue = evaluate(current);
                String msg = String.format(
                    "Iter %3d → curr=%.2f, best=%.2f",
                    iter, currentValue, bestValue
                );
                listener.onIteration(iter, msg);
            }
        }

        // 4) dump final solution
        String finalMsg = " Résultat final "
                        + "Meilleure sol : " + solutionToString(bestSolution) + "\n"
                        + String.format(
                            "Valeur = %.2f, Poids = %.2f/%.2f",
                            bestValue,
                            weight(bestSolution),
                            capacity
                          );
        listener.onIteration(maxIter, finalMsg);

        return bestSolution;
    }

    /** Convertit la solution binaire en chaîne lisible. */
    public String solutionToString(boolean[] sol) {
        StringBuilder sb = new StringBuilder("[");
        for (boolean b : sol) sb.append(b ? "1" : "0").append(" ");
        sb.append("]");
        return sb.toString();
    }
}

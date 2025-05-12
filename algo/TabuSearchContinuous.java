package algo;

import model.IterationListener;
import java.util.LinkedList;

public class TabuSearchContinuous {
    private int maxIter;
    private int tabuListSize;
    private LinkedList<Double> tabuList;

    public TabuSearchContinuous(int maxIter, int tabuListSize) {
        this.maxIter = maxIter;
        this.tabuListSize = tabuListSize;
        this.tabuList = new LinkedList<>();
    }

    /**
     * Optimise f(x)=x·cos(3πx) sur [start,end] par Recherche Tabou.
     * Ne notifie le listener qu'aux itérations 1, 2, 10 et maxIter.
     *
     * @param start    borne inférieure de la recherche
     * @param end      borne supérieure de la recherche
     * @param listener reçoit les messages d'itération
     * @return le x qui maximise f(x)
     */
    public double optimize(double start, double end, IterationListener listener) {
        // point initial aléatoire
        double bestX = start + Math.random() * (end - start);
        double bestVal = evaluate(bestX);

        // boucle principale
        for (int iter = 1; iter <= maxIter; iter++) {
            // génération d'un candidat dans le voisinage continu
            double candidate = bestX + (Math.random() - 0.5);
            candidate = Math.max(start, Math.min(end, candidate));

            // si déjà tabou, on passe
            if (tabuList.contains(candidate)) {
                continue;
            }

            // évaluation et mise à jour
            double candidateVal = evaluate(candidate);
            if (candidateVal > bestVal) {
                bestX = candidate;
                bestVal = candidateVal;
                tabuList.add(candidate);
                if (tabuList.size() > tabuListSize) {
                    tabuList.removeFirst();
                }
            }

            // notification uniquement aux itérations 1,2,10 et dernière
            if (iter == 1 || iter == 2 || iter == 10 || iter == maxIter) {
                listener.onIteration(
                        iter,
                        "Iteration " + iter +
                                " : best x = " + bestX +
                                ", f(x) = " + bestVal);
            }
        }

        return bestX;
    }

    /** Fonction à maximiser : f(x)=x·cos(3πx) */
    public double evaluate(double x) {
        return x * Math.cos(3 * Math.PI * x);
    }
}

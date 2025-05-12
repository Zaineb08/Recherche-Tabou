package algo;

import model.City;
import model.IterationListener;

import java.util.*;

/**
 * Recherche Tabou pour le problème du Voyageur de commerce (TSP).
 * Ne notifie le listener qu'aux itérations 1, 2, 10 et maxIter.
 */
public class TabuSearchTSP {
    private int maxIter;
    private int tabuListSize;
    private List<City> cities;
    private LinkedList<List<City>> tabuList;

    public TabuSearchTSP(int maxIter, int tabuListSize, List<City> cities) {
        this.maxIter = maxIter;
        this.tabuListSize = tabuListSize;
        this.cities = cities;
        this.tabuList = new LinkedList<>();
    }

    /** Calcule la distance totale d’une route (boucle fermée). */
    private double evaluate(List<City> route) {
        double distance = 0;
        for (int i = 0; i < route.size() - 1; i++) {
            distance += route.get(i).distanceTo(route.get(i + 1));
        }
        distance += route.get(route.size() - 1).distanceTo(route.get(0));
        return distance;
    }

    /** Génère une route initiale aléatoire. */
    private List<City> randomRoute() {
        List<City> route = new ArrayList<>(cities);
        Collections.shuffle(route);
        return route;
    }

    /** Copie profonde d’une route. */
    private List<City> copyRoute(List<City> route) {
        return new ArrayList<>(route);
    }

    /** Compare deux routes en fonction des IDs de ville. */
    private boolean routesEqual(List<City> r1, List<City> r2) {
        if (r1.size() != r2.size())
            return false;
        for (int i = 0; i < r1.size(); i++) {
            if (r1.get(i).id != r2.get(i).id)
                return false;
        }
        return true;
    }

    /** Génère tous les voisins par échange de deux villes (swap). */
    private List<List<City>> generateNeighbors(List<City> route) {
        List<List<City>> neighbors = new ArrayList<>();
        int n = route.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                List<City> neighbor = copyRoute(route);
                Collections.swap(neighbor, i, j);
                neighbors.add(neighbor);
            }
        }
        return neighbors;
    }

    /**
     * Lance la Recherche Tabou pour le TSP.
     * 
     * @param listener reçoit les notifications aux itérations clefs.
     * @return la meilleure route trouvée.
     */
    public List<City> optimize(IterationListener listener) {
        // 1) initialisation
        List<City> bestRoute = randomRoute();
        double bestDistance = evaluate(bestRoute);
        List<City> current = copyRoute(bestRoute);

        // 2) boucle principale
        for (int iter = 1; iter <= maxIter; iter++) {
            List<List<City>> neighbors = generateNeighbors(current);
            List<City> bestNeighbor = null;
            double bestNeighborDistance = Double.MAX_VALUE;

            for (List<City> neighbor : neighbors) {
                boolean isTabu = tabuList.stream().anyMatch(tsol -> routesEqual(neighbor, tsol));
                if (isTabu)
                    continue;

                double dist = evaluate(neighbor);
                if (dist < bestNeighborDistance) {
                    bestNeighbor = neighbor;
                    bestNeighborDistance = dist;
                }
            }

            // mise à jour du meilleur global
            if (bestNeighbor != null && bestNeighborDistance < bestDistance) {
                bestRoute = copyRoute(bestNeighbor);
                bestDistance = bestNeighborDistance;
            }

            // passage à la solution courante
            current = (bestNeighbor != null) ? bestNeighbor : current;

            // mise à jour de la liste Tabu
            tabuList.add(copyRoute(current));
            if (tabuList.size() > tabuListSize) {
                tabuList.removeFirst();
            }

            // notification aux itérations 1, 2, 10 et dernière
            if (iter == 1 || iter == 2 || iter == 10 || iter == maxIter) {
                listener.onIteration(
                        iter,
                        "Iteration " + iter + " : route distance = " + bestDistance);
            }
        }

        return bestRoute;
    }

    /** Convertit une route en chaîne d’IDs pour affichage. */
    public String routeToString(List<City> route) {
        StringBuilder sb = new StringBuilder();
        for (City city : route) {
            sb.append(city.id).append(" ");
        }
        return sb.toString().trim();
    }
}

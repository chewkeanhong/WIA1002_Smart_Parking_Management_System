package navigation;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

public class DijkstraPathfinder {

    // Helper class to track costs in the PriorityQueue
    private static class PathNode implements Comparable<PathNode> {
        String id;
        double cost;

        public PathNode(String id, double cost) {
            this.id = id;
            this.cost = cost;
        }

        @Override
        public int compareTo(PathNode other) {
            return Double.compare(this.cost, other.cost);
        }
    }

    public static List<String> findShortestPathToAvailableSpot(RouteGraph graph, String startEntranceId) {
        Map<String, Double> minCosts = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        PriorityQueue<PathNode> pq = new PriorityQueue<>();

        // 1. Initialize distances
        for (RouteGraph.Node node : graph.getAllNodes()) {
            minCosts.put(node.id, Double.MAX_VALUE);
        }

        minCosts.put(startEntranceId, 0.0);
        pq.add(new PathNode(startEntranceId, 0.0));
        
        String targetSpotId = null;

        // 2. Core Dijkstra Loop
        while (!pq.isEmpty()) {
            PathNode current = pq.poll();
            RouteGraph.Node currentNode = graph.getNode(current.id);

            // Goal Evaluation: Found the closest spot that is an available parking space!
            if (currentNode.isParkingSlot && !currentNode.isOccupied) {
                targetSpotId = currentNode.id;
                break;
            }

            // Skip processing if we found a shorter path to this node already
            if (current.cost > minCosts.get(current.id)) continue;

            // Check adjacent paths
            for (RouteGraph.Edge edge : graph.getNeighbors(current.id)) {
                RouteGraph.Node neighbor = edge.target;
                double alternateCost = minCosts.get(current.id) + edge.weight;

                if (alternateCost < minCosts.get(neighbor.id)) {
                    minCosts.put(neighbor.id, alternateCost);
                    predecessors.put(neighbor.id, current.id);
                    pq.add(new PathNode(neighbor.id, alternateCost));
                }
            }
        }

        // 3. Reconstruct path if a match was successfully found
        if (targetSpotId == null) {
            return Collections.emptyList(); // Returns empty if parking is completely full
        }

        LinkedList<String> finalPath = new LinkedList<>();
        String step = targetSpotId;
        while (step != null) {
            finalPath.addFirst(step);
            step = predecessors.get(step);
        }

        return finalPath;
    }

    /**
     * Dijkstra variant that terminates when a specific destination node is reached.
     * Returns the ordered list of node IDs from start to end, or an empty list if unreachable.
     */
    public static List<String> findShortestPath(RouteGraph graph, String startId, String endId) {
        if (graph.getNode(startId) == null || graph.getNode(endId) == null) {
            return Collections.emptyList();
        }

        Map<String, Double> minCosts = new HashMap<>();
        Map<String, String> predecessors = new HashMap<>();
        PriorityQueue<PathNode> pq = new PriorityQueue<>();

        for (RouteGraph.Node node : graph.getAllNodes()) {
            minCosts.put(node.id, Double.MAX_VALUE);
        }
        minCosts.put(startId, 0.0);
        pq.add(new PathNode(startId, 0.0));

        boolean reached = false;
        while (!pq.isEmpty()) {
            PathNode current = pq.poll();
            if (current.id.equals(endId)) { reached = true; break; }
            if (current.cost > minCosts.get(current.id)) continue;

            for (RouteGraph.Edge edge : graph.getNeighbors(current.id)) {
                RouteGraph.Node neighbor = edge.target;
                double alt = minCosts.get(current.id) + edge.weight;
                if (alt < minCosts.get(neighbor.id)) {
                    minCosts.put(neighbor.id, alt);
                    predecessors.put(neighbor.id, current.id);
                    pq.add(new PathNode(neighbor.id, alt));
                }
            }
        }

        if (!reached) return Collections.emptyList();

        LinkedList<String> path = new LinkedList<>();
        String step = endId;
        while (step != null) {
            path.addFirst(step);
            step = predecessors.get(step);
        }
        return path;
    }
}
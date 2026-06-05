package navigation;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.List;
import java.util.LinkedList;
import java.util.Collections;

// Runs Dijkstra's shortest-path algorithm over the RouteGraph to find
// either the nearest free slot or a route to a specific destination.
public class DijkstraPathfinder {

    // wraps a node + its current best cost so the priority queue can order them
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

        // start every node at "infinity" except the entrance
        for (RouteGraph.Node node : graph.getAllNodes()) {
            minCosts.put(node.id, Double.MAX_VALUE);
        }

        minCosts.put(startEntranceId, 0.0);
        pq.add(new PathNode(startEntranceId, 0.0));

        String targetSpotId = null;

        // keep pulling the cheapest node until we hit a free slot
        while (!pq.isEmpty()) {
            PathNode current = pq.poll();
            RouteGraph.Node currentNode = graph.getNode(current.id);

            // this is the first free parking slot we've reached, so it's the nearest one
            if (currentNode.isParkingSlot && !currentNode.isOccupied) {
                targetSpotId = currentNode.id;
                break;
            }

            // already found a cheaper way here, ignore this stale entry
            if (current.cost > minCosts.get(current.id)) continue;

            // relax the neighbours
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

        // nothing found means the lot is full
        if (targetSpotId == null) {
            return Collections.emptyList();
        }

        // walk the predecessors backwards to rebuild the route
        LinkedList<String> finalPath = new LinkedList<>();
        String step = targetSpotId;
        while (step != null) {
            finalPath.addFirst(step);
            step = predecessors.get(step);
        }

        return finalPath;
    }

    // same as above but stops once we reach a specific node.
    // gives back the node ids from start to end, or empty if there's no route.
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

    // add up the edge weights along a path we already have.
    // returns MAX_VALUE if the path is broken (some edge doesn't exist).
    public static int calculatePathCost(RouteGraph graph, List<String> path) {
        if (graph == null || path == null || path.size() < 2) {
            return 0;
        }

        double total = 0.0;
        for (int i = 0; i < path.size() - 1; i++) {
            RouteGraph.Node from = graph.getNode(path.get(i));
            RouteGraph.Node to = graph.getNode(path.get(i + 1));
            if (from == null || to == null) {
                return Integer.MAX_VALUE;
            }

            boolean found = false;
            for (RouteGraph.Edge edge : graph.getNeighbors(from.id)) {
                if (edge.target.id.equals(to.id)) {
                    total += edge.weight;
                    found = true;
                    break;
                }
            }
            if (!found) {
                return Integer.MAX_VALUE;
            }
        }

        return (int) Math.round(total);
    }

    // shortcut: find the shortest path and return just its cost
    public static int calculateShortestPathCost(RouteGraph graph, String startId, String endId) {
        return calculatePathCost(graph, findShortestPath(graph, startId, endId));
    }
}
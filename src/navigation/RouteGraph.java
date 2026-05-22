package navigation;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;

public class RouteGraph {
    
    // Inner class to represent structural nodes or individual parking bays
    public static class Node {
        public String id;
        public boolean isParkingSlot;
        public boolean isOccupied;

        public Node(String id, boolean isParkingSlot) {
            this.id = id;
            this.isParkingSlot = isParkingSlot;
            this.isOccupied = false; 
        }
    }

    // Inner class for pathways connecting nodes
    public static class Edge {
        public Node target;
        public double weight; // Physical distance / travel time cost

        public Edge(Node target, double weight) {
            this.target = target;
            this.weight = weight;
        }
    }

    private final Map<String, Node> nodes = new HashMap<>();
    private final Map<String, List<Edge>> adjacencyList = new HashMap<>();

    public RouteGraph() {
        // Keeps instantiation flexible
    }

    /**
     * Natively builds the massive 174-slot multi-stack mall car park grid layout.
     * Keeps all 4 corners clear of slots to act strictly as transit junctions.
     */
    public void initializeLargeMallLayout() {
        // 1. INITIALIZE TRANSIT SPACES & TURNING CORNERS (isParkingSlot = false)
        this.addNode("MAIN_ENTRANCE", false);
        this.addNode("MAIN_EXIT", false);
        
        // Blank Corners (Safe turning radiuses - NO PARKING STALLS ALLOWED)
        this.addNode("CORNER_NW", false); 
        this.addNode("CORNER_NE", false); 
        this.addNode("CORNER_SW", false); 
        this.addNode("CORNER_SE", false); 
        
        // Primary Driving Aisle Thoroughfares
        this.addNode("NORTH_DRIVE_LANE", false);  // Back Wall Lane
        this.addNode("SOUTH_DRIVE_LANE", false);  // Front Entrance/Exit Lane
        this.addNode("EAST_DRIVE_LANE", false);   // Right Wall Lane
        this.addNode("WEST_DRIVE_LANE", false);   // Left Wall Lane
        this.addNode("CENTER_CROSS_AISLE", false); // Main middle driving cross-spine


        // 2. PROGRAMMATICALLY GENERATE THE WALL SLOTS (Perimeters)
        // West (Left) Wall: 10 Slots (W_WALL_01 to W_WALL_10)
        for (int i = 1; i <= 10; i++) {
            this.addNode("W_WALL_" + String.format("%02d", i), true);
        }
        
        // East (Right) Wall: 10 Slots (E_WALL_01 to E_WALL_10)
        for (int i = 1; i <= 10; i++) {
            this.addNode("E_WALL_" + String.format("%02d", i), true);
        }
        
        // Back (North) Wall: 10 Slots (B_WALL_01 to B_WALL_10)
        for (int i = 1; i <= 10; i++) {
            this.addNode("B_WALL_" + String.format("%02d", i), true);
        }


        // 3. PROGRAMMATICALLY GENERATE THE 6 MIDDLE STACKS (Back-to-Back Rows)
        // 6 distinct island stacks. Each stack has Row A (12 slots) and Row B (12 slots)
        for (int stack = 1; stack <= 6; stack++) {
            for (int slot = 1; slot <= 12; slot++) {
                this.addNode("STK" + stack + "_A_" + String.format("%02d", slot), true);
                this.addNode("STK" + stack + "_B_" + String.format("%02d", slot), true);
            }
        }


        // 4. CONNECT THE MAIN TRAFFIC CIRCUITS (Outer Loop Grid Infrastructure)
        this.addEdge("MAIN_ENTRANCE", "SOUTH_DRIVE_LANE", 2.0);
        this.addEdge("SOUTH_DRIVE_LANE", "CORNER_SW", 3.0);
        this.addEdge("CORNER_SW", "WEST_DRIVE_LANE", 4.0);
        this.addEdge("WEST_DRIVE_LANE", "CORNER_NW", 12.0);
        
        this.addEdge("CORNER_NW", "NORTH_DRIVE_LANE", 4.0);
        this.addEdge("NORTH_DRIVE_LANE", "CORNER_NE", 12.0);
        this.addEdge("CORNER_NE", "EAST_DRIVE_LANE", 4.0);
        this.addEdge("EAST_DRIVE_LANE", "CORNER_SE", 12.0);
        
        this.addEdge("CORNER_SE", "SOUTH_DRIVE_LANE", 3.0);
        this.addEdge("SOUTH_DRIVE_LANE", "MAIN_EXIT", 2.0);

        // Connect the outer perimeter drive loop to the central crossing aisle
        this.addEdge("WEST_DRIVE_LANE", "CENTER_CROSS_AISLE", 6.0);
        this.addEdge("CENTER_CROSS_AISLE", "EAST_DRIVE_LANE", 6.0);


        // 5. MAP THE BAYS NATIVELY TO THEIR NEAREST DRIVING PATHWAYS
        // Link Left Wall Slots to the West Drive Lane
        for (int i = 1; i <= 10; i++) {
            this.addEdge("WEST_DRIVE_LANE", "W_WALL_" + String.format("%02d", i), 1.5);
        }
        
        // Link Right Wall Slots to the East Drive Lane
        for (int i = 1; i <= 10; i++) {
            this.addEdge("EAST_DRIVE_LANE", "E_WALL_" + String.format("%02d", i), 1.5);
        }
        
        // Link Back Wall Slots to the North Drive Lane
        for (int i = 1; i <= 10; i++) {
            this.addEdge("NORTH_DRIVE_LANE", "B_WALL_" + String.format("%02d", i), 1.5);
        }

        // Link the 6 dense central island double-stacks to corresponding driving streams
        for (int stack = 1; stack <= 6; stack++) {
            for (int slot = 1; slot <= 12; slot++) {
                String rowASlot = "STK" + stack + "_A_" + String.format("%02d", slot);
                String rowBSlot = "STK" + stack + "_B_" + String.format("%02d", slot);
                
                // Stacks 1, 2, 3 sit in the upper half (accessible from North and Center lanes)
                if (stack <= 3) {
                    this.addEdge("NORTH_DRIVE_LANE", rowASlot, 2.0);
                    this.addEdge("CENTER_CROSS_AISLE", rowBSlot, 2.5);
                } 
                // Stacks 4, 5, 6 sit in the lower half (accessible from Center and South lanes)
                else {
                    this.addEdge("CENTER_CROSS_AISLE", rowASlot, 2.5);
                    this.addEdge("SOUTH_DRIVE_LANE", rowBSlot, 2.0);
                }
            }
        }
    }

    // --- STANDARD GRAPH APIS ---
    public void addNode(String id, boolean isParkingSlot) {
        Node node = new Node(id, isParkingSlot);
        nodes.put(id, node);
        adjacencyList.put(id, new ArrayList<>());
    }

    public void addEdge(String sourceId, String targetId, double weight) {
        Node source = nodes.get(sourceId);
        Node target = nodes.get(targetId);
        if (source != null && target != null) {
            adjacencyList.get(sourceId).add(new Edge(target, weight));
        }
    }

    public Node getNode(String id) {
        return nodes.get(id);
    }

    public List<Edge> getNeighbors(String id) {
        return adjacencyList.getOrDefault(id, Collections.emptyList());
    }

    public Collection<Node> getAllNodes() {
        return nodes.values();
    }
    
    public void setOccupancy(String id, boolean isOccupied) {
        if (nodes.containsKey(id)) {
            nodes.get(id).isOccupied = isOccupied;
        }
    }
}
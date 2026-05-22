package navigation;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;

public class RouteGraph {
    
    // Inner class to represent structural elements or slots
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

    // Inner class for pathways
    public static class Edge {
        public Node target;
        public double weight; // Travel cost / distance

        public Edge(Node target, double weight) {
            this.target = target;
            this.weight = weight;
        }
    }

    private final Map<String, Node> nodes = new HashMap<>();
    private final Map<String, List<Edge>> adjacencyList = new HashMap<>();

    // Standard constructor
    public RouteGraph() {
        // Keeps it flexible if you want an empty graph
    }

    // Explicit method to build your large layout setup instantly
    public void initializeLargeMallLayout() {
        // 1. SET UP TRANSIT SPACES & CORNERS (isParkingSlot = false)
        this.addNode("MAIN_ENTRANCE", false);
        this.addNode("MAIN_EXIT", false);
        
        // Corner Nodes - kept clear of slots to allow safe turning radiuses
        this.addNode("CORNER_BL", false); // Bottom Left
        this.addNode("CORNER_TL", false); // Top Left
        this.addNode("CORNER_TR", false); // Top Right
        this.addNode("CORNER_BR", false); // Bottom Right
        
        // Main Driving Thoroughfares
        this.addNode("LEFT_DRIVE_LANE", false);
        this.addNode("RIGHT_DRIVE_LANE", false);
        this.addNode("CENTRAL_DRIVE_LANE", false);
        this.addNode("BACK_DRIVE_LANE", false);

        // 2. PROGRAMMATICALLY GENERATE LARGE PARKING ROWS
        // Zone A (Middle-Left Block): 24 slots (ZA01 to ZA24)
        for (int i = 1; i <= 24; i++) {
            this.addNode("ZA" + String.format("%02d", i), true);
        }
        // Zone B (Middle-Right Block): 24 slots (ZB01 to ZB24)
        for (int i = 1; i <= 24; i++) {
            this.addNode("ZB" + String.format("%02d", i), true);
        }
        // Outer Perimeters (Left & Right Wall Slots): 12 slots each
        for (int i = 1; i <= 12; i++) {
            this.addNode("L_WALL_" + String.format("%02d", i), true);
            this.addNode("R_WALL_" + String.format("%02d", i), true);
        }

        // 3. CONNECT THE DRIVING VEINS (Primary Loop Topology)
        this.addEdge("MAIN_ENTRANCE", "CORNER_BL", 4.0);
        this.addEdge("CORNER_BL", "LEFT_DRIVE_LANE", 5.0);
        this.addEdge("LEFT_DRIVE_LANE", "CORNER_TL", 8.0);
        this.addEdge("CORNER_TL", "BACK_DRIVE_LANE", 6.0);
        this.addEdge("BACK_DRIVE_LANE", "CENTRAL_DRIVE_LANE", 4.0);
        this.addEdge("BACK_DRIVE_LANE", "CORNER_TR", 6.0);
        this.addEdge("CORNER_TR", "RIGHT_DRIVE_LANE", 8.0);
        this.addEdge("RIGHT_DRIVE_LANE", "CORNER_BR", 5.0);
        this.addEdge("CORNER_BR", "MAIN_EXIT", 4.0);

        // 4. ATTACH THE LARGE SLOTS PACKS TO DRIVE LANES
        // Left wall slots link to Left lane
        for (int i = 1; i <= 12; i++) {
            this.addEdge("LEFT_DRIVE_LANE", "L_WALL_" + String.format("%02d", i), 1.5);
        }
        // Right wall slots link to Right lane
        for (int i = 1; i <= 12; i++) {
            this.addEdge("RIGHT_DRIVE_LANE", "R_WALL_" + String.format("%02d", i), 1.5);
        }
        // Zone A Slots are packed in the middle, accessible from Left and Central lanes
        for (int i = 1; i <= 24; i++) {
            String slotId = "ZA" + String.format("%02d", i);
            this.addEdge("LEFT_DRIVE_LANE", slotId, 2.0);
            this.addEdge("CENTRAL_DRIVE_LANE", slotId, 2.0);
        }
        // Zone B Slots are packed in the middle, accessible from Central and Right lanes
        for (int i = 1; i <= 24; i++) {
            String slotId = "ZB" + String.format("%02d", i);
            this.addEdge("CENTRAL_DRIVE_LANE", slotId, 2.0);
            this.addEdge("RIGHT_DRIVE_LANE", slotId, 2.0);
        }
    }

    // Basic Graph API Methods
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
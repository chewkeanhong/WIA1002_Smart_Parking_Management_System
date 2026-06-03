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
        public double x;
        public double y;

        public Node(String id, boolean isParkingSlot) {
            this.id = id;
            this.isParkingSlot = isParkingSlot;
            this.isOccupied = false;
        }

        public Node(String id, boolean isParkingSlot, double x, double y) {
            this(id, isParkingSlot);
            this.x = x;
            this.y = y;
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

    /**
     * Builds a graph that mirrors the dashboard's 40-slot lot — two 2×10 blocks
     * separated by a central driving aisle, framed by roads on all four sides.
     * Slot nodes are named A01–A40 (matching {@code ParkingMap.slotId}) so that
     * a vehicle's {@code assignedSlotId} can be used directly as a destination.
     *
     * Coordinates are in logical pixel units (the canvas scales them to fit).
     */
    public void initializeDashboardLayout() {
        // Slot column x-positions (10 cols) and the canvas frame y-positions.
        double[] colX = new double[10];
        double leftEdge = 110, rightEdge = 890;
        for (int i = 0; i < 10; i++) {
            colX[i] = leftEdge + i * ((rightEdge - leftEdge) / 9.0);
        }
        double yTopRoad   =  60;
        double yBlk1Row0  = 150;
        double yBlk1Row1  = 230;
        double yMidAisle  = 320;
        double yBlk2Row0  = 410;
        double yBlk2Row1  = 490;
        double yBotRoad   = 580;
        double xLeftRoad  =  50;
        double xRightRoad = 950;

        // 1. Road junction nodes
        addNode("NW", false, xLeftRoad,  yTopRoad);
        addNode("NE", false, xRightRoad, yTopRoad);
        addNode("SW", false, xLeftRoad,  yBotRoad);
        addNode("SE", false, xRightRoad, yBotRoad);
        addNode("GATE_A", false, xLeftRoad,  yMidAisle);  // mid-left  (label)
        addNode("GATE_B", false, xRightRoad, yMidAisle);  // mid-right (label)
        addNode("GATE_C", false, 500.0,     yTopRoad);    // top-center (label)
        addNode("ENTRANCE", false, 470.0,   yBotRoad);
        addNode("EXIT",     false, 530.0,   yBotRoad);

        // Per-column waypoints on top road, mid aisle, bottom road.
        for (int i = 0; i < 10; i++) {
            addNode("TOP_C" + i, false, colX[i], yTopRoad);
            addNode("MID_C" + i, false, colX[i], yMidAisle);
            addNode("BOT_C" + i, false, colX[i], yBotRoad);
        }

        // 2. Slot nodes (A01–A40) positioned at their cell centers.
        for (int i = 0; i < 40; i++) {
            int col = i % 10;
            double sy;
            int row = i / 10;
            switch (row) {
                case 0:  sy = yBlk1Row0; break;
                case 1:  sy = yBlk1Row1; break;
                case 2:  sy = yBlk2Row0; break;
                default: sy = yBlk2Row1; break;
            }
            String id = String.format("A%02d", i + 1);
            addNode(id, true, colX[col], sy);
        }

        // 3. Edges: roads form a connected frame + central aisle.
        // Top road: NW – GATE_C(via TOP_C nodes) – NE
        linkChain("NW", "TOP_C0");
        for (int i = 0; i < 9; i++) linkChain("TOP_C" + i, "TOP_C" + (i + 1));
        linkChain("TOP_C9", "NE");
        // Bottom road: SW – ENTRANCE/EXIT – SE
        linkChain("SW", "BOT_C0");
        for (int i = 0; i < 9; i++) linkChain("BOT_C" + i, "BOT_C" + (i + 1));
        linkChain("BOT_C9", "SE");
        // Entrance / exit hook onto nearest column waypoints (BOT_C4 ≈ x 461, BOT_C5 ≈ x 548).
        linkChain("BOT_C4", "ENTRANCE");
        linkChain("ENTRANCE", "BOT_C5");
        linkChain("BOT_C4", "EXIT");
        linkChain("EXIT",     "BOT_C5");
        // Side roads (with GATE_A / GATE_B sitting on them mid-way).
        linkChain("NW", "GATE_A");
        linkChain("GATE_A", "SW");
        linkChain("NE", "GATE_B");
        linkChain("GATE_B", "SE");
        // Central aisle
        linkChain("GATE_A", "MID_C0");
        for (int i = 0; i < 9; i++) linkChain("MID_C" + i, "MID_C" + (i + 1));
        linkChain("MID_C9", "GATE_B");
        // Gate C sits on top road between TOP_C4 and TOP_C5.
        linkChain("TOP_C4", "GATE_C");
        linkChain("GATE_C", "TOP_C5");

        // 4. Each slot connects to its nearest road waypoint.
        for (int i = 0; i < 40; i++) {
            String id = String.format("A%02d", i + 1);
            int col = i % 10;
            int row = i / 10;
            switch (row) {
                case 0: linkChain("TOP_C" + col, id); break; // block 1, row 0 → top road
                case 1: linkChain("MID_C" + col, id); break; // block 1, row 1 → middle aisle
                case 2: linkChain("MID_C" + col, id); break; // block 2, row 0 → middle aisle
                default: linkChain("BOT_C" + col, id); break; // block 2, row 1 → bottom road
            }
        }
    }

    /** Adds bidirectional edges between two nodes, weighted by Euclidean distance. */
    private void linkChain(String a, String b) {
        Node na = nodes.get(a);
        Node nb = nodes.get(b);
        if (na == null || nb == null) return;
        double dx = na.x - nb.x;
        double dy = na.y - nb.y;
        double w  = Math.sqrt(dx * dx + dy * dy);
        addEdge(a, b, w);
        addEdge(b, a, w);
    }

    // --- STANDARD GRAPH APIS ---
    public void addNode(String id, boolean isParkingSlot) {
        Node node = new Node(id, isParkingSlot);
        nodes.put(id, node);
        adjacencyList.put(id, new ArrayList<>());
    }

    public void addNode(String id, boolean isParkingSlot, double x, double y) {
        Node node = new Node(id, isParkingSlot, x, y);
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

    public void clearOccupancy() {
        for (Node node : nodes.values()) {
            if (node.isParkingSlot) {
                node.isOccupied = false;
            }
        }
    }
}
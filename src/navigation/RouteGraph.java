package navigation;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Collection;

// Graph of the car park. Nodes are either driving waypoints or actual
// parking bays, and edges are the drivable paths between them with a
// distance weight. Dijkstra runs over this to find routes.
public class RouteGraph {

    // a point in the lot - either a parking bay or just a road junction
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

    // a path from one node to another
    public static class Edge {
        public Node target;
        public double weight; // distance / travel cost

        public Edge(Node target, double weight) {
            this.target = target;
            this.weight = weight;
        }
    }

    private final Map<String, Node> nodes = new HashMap<>();
    private final Map<String, List<Edge>> adjacencyList = new HashMap<>();

    public RouteGraph() {
    }

    // Builds a big 174-slot mall-style layout. The four corners are left as
    // plain junctions (no parking) so cars have room to turn.
    public void initializeLargeMallLayout() {
        // entrance, exit and the corner junctions (not parking slots)
        this.addNode("MAIN_ENTRANCE", false);
        this.addNode("MAIN_EXIT", false);

        this.addNode("CORNER_NW", false);
        this.addNode("CORNER_NE", false);
        this.addNode("CORNER_SW", false);
        this.addNode("CORNER_SE", false);

        // the main driving lanes around and through the lot
        this.addNode("NORTH_DRIVE_LANE", false);  // back wall lane
        this.addNode("SOUTH_DRIVE_LANE", false);  // front entrance/exit lane
        this.addNode("EAST_DRIVE_LANE", false);   // right wall lane
        this.addNode("WEST_DRIVE_LANE", false);   // left wall lane
        this.addNode("CENTER_CROSS_AISLE", false); // middle cross aisle


        // wall slots around the perimeter, 10 on each of three walls
        for (int i = 1; i <= 10; i++) {
            this.addNode("W_WALL_" + String.format("%02d", i), true);
        }
        for (int i = 1; i <= 10; i++) {
            this.addNode("E_WALL_" + String.format("%02d", i), true);
        }
        for (int i = 1; i <= 10; i++) {
            this.addNode("B_WALL_" + String.format("%02d", i), true);
        }


        // 6 island stacks in the middle, each with two rows (A and B) of 12
        for (int stack = 1; stack <= 6; stack++) {
            for (int slot = 1; slot <= 12; slot++) {
                this.addNode("STK" + stack + "_A_" + String.format("%02d", slot), true);
                this.addNode("STK" + stack + "_B_" + String.format("%02d", slot), true);
            }
        }


        // wire up the outer loop of driving lanes
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

        // hook the outer loop into the center aisle
        this.addEdge("WEST_DRIVE_LANE", "CENTER_CROSS_AISLE", 6.0);
        this.addEdge("CENTER_CROSS_AISLE", "EAST_DRIVE_LANE", 6.0);


        // connect each wall of slots to the lane running next to it
        for (int i = 1; i <= 10; i++) {
            this.addEdge("WEST_DRIVE_LANE", "W_WALL_" + String.format("%02d", i), 1.5);
        }
        for (int i = 1; i <= 10; i++) {
            this.addEdge("EAST_DRIVE_LANE", "E_WALL_" + String.format("%02d", i), 1.5);
        }
        for (int i = 1; i <= 10; i++) {
            this.addEdge("NORTH_DRIVE_LANE", "B_WALL_" + String.format("%02d", i), 1.5);
        }

        // connect the middle stacks to whichever lanes they sit between
        for (int stack = 1; stack <= 6; stack++) {
            for (int slot = 1; slot <= 12; slot++) {
                String rowASlot = "STK" + stack + "_A_" + String.format("%02d", slot);
                String rowBSlot = "STK" + stack + "_B_" + String.format("%02d", slot);

                // stacks 1-3 are up top (north + center lanes)
                if (stack <= 3) {
                    this.addEdge("NORTH_DRIVE_LANE", rowASlot, 2.0);
                    this.addEdge("CENTER_CROSS_AISLE", rowBSlot, 2.5);
                }
                // stacks 4-6 are down bottom (center + south lanes)
                else {
                    this.addEdge("CENTER_CROSS_AISLE", rowASlot, 2.5);
                    this.addEdge("SOUTH_DRIVE_LANE", rowBSlot, 2.0);
                }
            }
        }
    }

    // Builds the smaller layout that matches the dashboard map: two 2x10
    // blocks with an aisle down the middle and roads around the edges.
    // Slots are named A01-A40 to match ParkingMap, so a vehicle's
    // assignedSlotId can be used straight away as the destination.
    // Coordinates are in logical pixels - the canvas scales them to fit.
    public void initializeDashboardLayout() {
        // x for each of the 10 columns, plus the y of each road/row
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

        // road junctions and the three gate labels
        addNode("NW", false, xLeftRoad,  yTopRoad);
        addNode("NE", false, xRightRoad, yTopRoad);
        addNode("SW", false, xLeftRoad,  yBotRoad);
        addNode("SE", false, xRightRoad, yBotRoad);
        addNode("GATE_A", false, xLeftRoad,  yMidAisle);  // mid-left
        addNode("GATE_B", false, xRightRoad, yMidAisle);  // mid-right
        addNode("GATE_C", false, 500.0,     yTopRoad);    // top-center
        addNode("ENTRANCE", false, 470.0,   yBotRoad);
        addNode("EXIT",     false, 530.0,   yBotRoad);

        // one waypoint per column on the top road, mid aisle and bottom road
        for (int i = 0; i < 10; i++) {
            addNode("TOP_C" + i, false, colX[i], yTopRoad);
            addNode("MID_C" + i, false, colX[i], yMidAisle);
            addNode("BOT_C" + i, false, colX[i], yBotRoad);
        }

        // the 40 actual parking slots, each at the center of its cell
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

        // now the roads. top road runs left to right through the TOP_C waypoints.
        linkChain("NW", "TOP_C0");
        for (int i = 0; i < 9; i++) linkChain("TOP_C" + i, "TOP_C" + (i + 1));
        linkChain("TOP_C9", "NE");
        // bottom road, same idea
        linkChain("SW", "BOT_C0");
        for (int i = 0; i < 9; i++) linkChain("BOT_C" + i, "BOT_C" + (i + 1));
        linkChain("BOT_C9", "SE");
        // entrance/exit tap into the nearest bottom waypoints
        linkChain("BOT_C4", "ENTRANCE");
        linkChain("ENTRANCE", "BOT_C5");
        linkChain("BOT_C4", "EXIT");
        linkChain("EXIT",     "BOT_C5");
        // the two side roads, with gates A and B sitting halfway along them
        linkChain("NW", "GATE_A");
        linkChain("GATE_A", "SW");
        linkChain("NE", "GATE_B");
        linkChain("GATE_B", "SE");
        // the middle aisle
        linkChain("GATE_A", "MID_C0");
        for (int i = 0; i < 9; i++) linkChain("MID_C" + i, "MID_C" + (i + 1));
        linkChain("MID_C9", "GATE_B");
        // gate C sits on the top road
        linkChain("TOP_C4", "GATE_C");
        linkChain("GATE_C", "TOP_C5");

        // finally, hook each slot up to the nearest bit of road
        for (int i = 0; i < 40; i++) {
            String id = String.format("A%02d", i + 1);
            int col = i % 10;
            int row = i / 10;
            switch (row) {
                case 0: linkChain("TOP_C" + col, id); break; // top row -> top road
                case 1: linkChain("MID_C" + col, id); break; // -> middle aisle
                case 2: linkChain("MID_C" + col, id); break; // -> middle aisle
                default: linkChain("BOT_C" + col, id); break; // bottom row -> bottom road
            }
        }
    }

    // add edges both ways between two nodes, weighted by the straight-line distance
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

    // --- basic graph operations ---
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
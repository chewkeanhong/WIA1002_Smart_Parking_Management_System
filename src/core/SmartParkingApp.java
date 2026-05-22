package core;

import java.util.List;

public class SmartParkingApp {
    public static void main(String[] args) {
        
        // Use the explicit folder prefix to instantiate your class
        navigation.RouteGraph lot = new navigation.RouteGraph();
        lot.initializeLargeMallLayout();
        
        // Call your pathfinder method
        List<String> path = navigation.DijkstraPathfinder.findShortestPathToAvailableSpot(lot, "MAIN_ENTRANCE");
        
        if (!path.isEmpty()) {
            System.out.println("Path: " + String.join(" -> ", path));
        } else {
            System.out.println("Lot Full!");
        }
    }
}
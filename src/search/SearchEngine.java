package search;

import models.Vehicle;
import java.util.List;

/** Facade over VehicleBST for the search module. */
public class SearchEngine {

    private final VehicleBST bst = new VehicleBST();

    public void    addVehicle(Vehicle v)         { bst.insert(v); }
    public Vehicle findVehicle(String plate)     { return bst.search(plate); }
    public boolean removeVehicle(String plate)   { return bst.delete(plate); }
    public List<Vehicle> getSortedVehicles()     { return bst.inOrder(); }
    public VehicleBST    getTree()               { return bst; }
}

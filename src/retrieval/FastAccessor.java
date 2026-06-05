package retrieval;

import models.Vehicle;
import models.ParkingSlot;

// Thin wrapper over our HashMaps so we can grab a vehicle or slot by key
// instantly instead of looping through a list. Keeps three caches:
// plate -> vehicle, slotId -> slot, and plate -> slotId.
public class FastAccessor {

    private final HashMap<String, Vehicle>    vehicleMap    = new HashMap<>(64);
    private final HashMap<String, ParkingSlot> slotMap      = new HashMap<>(64);
    private final HashMap<String, String>      vehicleToSlot = new HashMap<>(64);

    // vehicles, keyed by plate
    public void cacheVehicle(Vehicle v) {
        vehicleMap.put(v.getLicensePlate(), v);
    }

    public Vehicle getVehicle(String plate) {
        return vehicleMap.get(plate);
    }

    public boolean removeVehicle(String plate) {
        return vehicleMap.remove(plate);
    }

    // slots, keyed by slot id
    public void cacheSlot(ParkingSlot s)  {
        slotMap.put(s.getSlotId(), s);
    }

    public ParkingSlot getSlot(String slotId) {
        return slotMap.get(slotId);
    }

    public boolean removeSlot(String slotId) {
        return slotMap.remove(slotId);
    }

    // remember which slot a vehicle is parked in
    public void mapVehicleToSlot(String plate, String slotId) {
        vehicleToSlot.put(plate, slotId);
    }

    public String getSlotForVehicle(String plate) {
        return vehicleToSlot.get(plate);
    }

    // the UI tables read straight from these
    public HashMap<String, Vehicle> getVehicleMap() {
        return vehicleMap;
    }

    public HashMap<String, ParkingSlot> getSlotMap() {
        return slotMap;
    }

    public void clear() {
        vehicleMap.clear();
        slotMap.clear();
        vehicleToSlot.clear();
    }
}

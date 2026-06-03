package retrieval;

import models.Vehicle;
import models.ParkingSlot;

/**
 * O(1) lookup facade backed by custom HashMaps.
 * Caches vehicles by license plate and slots by slot ID.
 */
public class FastAccessor {

    private final HashMap<String, Vehicle> vehicleMap = new HashMap<>();
    private final HashMap<String, ParkingSlot> slotMap = new HashMap<>();
    private final HashMap<String, String> vehicleToSlot = new HashMap<>();

    // 1. Vehicle cache 
    // Store or update a vehicle using its license plate as the key.
    public void cacheVehicle(Vehicle v) { 
        vehicleMap.put(v.getLicensePlate(), v); 
    }

    // Fetch a vehicle directly by plate without scanning a list.
    public Vehicle getVehicle(String plate) { 
        return vehicleMap.get(plate); 
    }

    // Remove a cached vehicle if it is no longer needed.
    public boolean removeVehicle(String plate) { 
        return vehicleMap.remove(plate); 
    }

    // 2. Slot cache
    // Store or update a parking slot using its slot ID as the key.
    public void cacheSlot(ParkingSlot s)  { 
        slotMap.put(s.getSlotId(), s); 
    }

    // Fetch a slot directly by slot ID.
    public ParkingSlot getSlot(String slotId) { 
        return slotMap.get(slotId); 
    }

    // Vehicle → Slot mapping
    // Record which slot a vehicle is currently assigned to.
    public void mapVehicleToSlot(String plate, String slotId) { 
        vehicleToSlot.put(plate, slotId); 
    }

    // Look up the slot ID assigned to a vehicle plate.
    public String getSlotForVehicle(String plate) { 
        return vehicleToSlot.get(plate); 
    }

    // Expose the underlying caches for UI tables or debugging views.
    public HashMap<String, Vehicle> getVehicleMap() { 
        return vehicleMap; 
    }

    // Expose the slot cache for UI tables or debugging views.
    public HashMap<String, ParkingSlot> getSlotMap() { 
        return slotMap; 
    }

    // Clear all cached retrieval data.
    public void clear() {
        vehicleMap.clear();
        slotMap.clear();
        vehicleToSlot.clear();
    }
}

package retrieval;

import models.Vehicle;
import models.ParkingSlot;

/**
 * O(1) lookup facade backed by custom HashMaps.
 * Caches vehicles by license plate and slots by slot ID.
 */
public class FastAccessor {

    private final HashMap<String, Vehicle>    vehicleMap     = new HashMap<>();
    private final HashMap<String, ParkingSlot> slotMap       = new HashMap<>();
    private final HashMap<String, String>      vehicleToSlot = new HashMap<>();

    // ── Vehicle cache ─────────────────────────────────────────────────────────
    public void    cacheVehicle(Vehicle v)       { vehicleMap.put(v.getLicensePlate(), v); }
    public Vehicle getVehicle(String plate)      { return vehicleMap.get(plate); }
    public boolean removeVehicle(String plate)   { return vehicleMap.remove(plate); }

    // ── Slot cache ────────────────────────────────────────────────────────────
    public void        cacheSlot(ParkingSlot s)  { slotMap.put(s.getSlotId(), s); }
    public ParkingSlot getSlot(String slotId)    { return slotMap.get(slotId); }

    // ── Vehicle → Slot mapping ────────────────────────────────────────────────
    public void   mapVehicleToSlot(String plate, String slotId) { vehicleToSlot.put(plate, slotId); }
    public String getSlotForVehicle(String plate)               { return vehicleToSlot.get(plate); }

    public HashMap<String, Vehicle>    getVehicleMap() { return vehicleMap; }
    public HashMap<String, ParkingSlot> getSlotMap()   { return slotMap; }
}

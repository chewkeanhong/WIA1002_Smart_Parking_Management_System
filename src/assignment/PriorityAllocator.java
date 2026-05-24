package assignment;

import models.ParkingSlot;
import models.Vehicle;

/**
 * Manages slot assignment via a Min-Heap (Priority Queue).
 * Assigns the nearest available slot in O(log n) — vs O(n) linear scan.
 */
public class PriorityAllocator {

    private final SlotMinHeap heap;

    public PriorityAllocator() { heap = new SlotMinHeap(); }

    public void clearSlots() { heap.clear(); }

    /** Add an unoccupied slot into the priority queue. */
    public void addSlot(ParkingSlot slot) {
        if (!slot.isOccupied()) heap.insert(slot);
    }

    /**
     * Assign the nearest available slot to the given vehicle — O(log n).
     * Returns null if no slots remain.
     */
    public ParkingSlot assignBestSlot(Vehicle vehicle) {
        ParkingSlot slot = heap.pollMin();
        if (slot != null) {
            slot.setParkedVehicle(vehicle);
            vehicle.setAssignedSlotId(slot.getSlotId());
        }
        return slot;
    }

    public ParkingSlot  peekBestSlot()     { return heap.peekMin();  }
    public SlotMinHeap  getHeap()          { return heap;            }
    public boolean      hasAvailableSlots(){ return !heap.isEmpty(); }
    public int          availableCount()   { return heap.getSize();  }
}

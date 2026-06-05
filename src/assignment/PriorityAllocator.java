package assignment;

import models.ParkingSlot;
import models.Vehicle;

// Wraps the min-heap so the rest of the app can just ask for the
// "best" (nearest) slot without touching the heap internals.
public class PriorityAllocator {

    private final SlotMinHeap heap;

    public PriorityAllocator() {
        heap = new SlotMinHeap();
    }

    public void clearSlots() {
        heap.clear();
    }

    // only free slots go into the heap; skip occupied ones
    public void addSlot(ParkingSlot slot) {
        if (!slot.isOccupied())
            heap.insert(slot);
    }

    // pull the nearest free slot and park the vehicle in it.
    // returns null if nothing's left.
    public ParkingSlot assignBestSlot(Vehicle vehicle) {
        ParkingSlot slot = heap.pollMin();
        if (slot != null) {
            slot.setParkedVehicle(vehicle);
            vehicle.setAssignedSlotId(slot.getSlotId());
        }
        return slot;
    }

    public ParkingSlot peekBestSlot() {
        return heap.peekMin();
    }

    // the UI needs the heap to draw the tree / table
    public SlotMinHeap getHeap() {
        return heap;
    }

    public boolean hasAvailableSlots() {
        return !heap.isEmpty();
    }

    public int availableCount() {
        return heap.getSize();
    }
}

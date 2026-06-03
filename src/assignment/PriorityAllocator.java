package assignment;

import models.ParkingSlot;
import models.Vehicle;

/**
 * Manages slot assignment via a Min-Heap (Priority Queue).
 * Assigns the nearest available slot in O(log n) — vs O(n) linear scan.
 */
public class PriorityAllocator {

    private final SlotMinHeap heap;

    // Create the allocator with an empty priority queue.
    public PriorityAllocator() { 
        heap = new SlotMinHeap(); 
    }

    // Remove all slots before rebuilding the queue for a new gate selection.
    public void clearSlots() { 
        heap.clear(); 
    }

    // Add an unoccupied slot into the priority queue. Occupied slots are ignored.
    public void addSlot(ParkingSlot slot) {
        if (!slot.isOccupied()) 
            heap.insert(slot);
    }

    /**
     * Remove the best slot from the heap and assign it to the vehicle.
     * The heap root is always the smallest distance-to-gate value.
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

    // Look at the best slot without removing it from the heap. 
    public ParkingSlot  peekBestSlot() { 
        return heap.peekMin();  
    }

    // Expose the heap for visualisation and table rendering in the UI.
    public SlotMinHeap  getHeap() { 
        return heap;            
    }

    // Check whether the queue still has at least one available slot.
    public boolean hasAvailableSlots() { 
        return !heap.isEmpty(); 
    }

    // Number of slots currently stored in the queue.
    public int availableCount() { 
        return heap.getSize();  
    }
}

package assignment;

import models.ParkingSlot;

/**
 * Binary Min-Heap ordered by distanceToGate (1-indexed array).
 * Insert: O(log n)  |  Poll min: O(log n)  |  Peek: O(1)
 * Contrast with linear search: O(n) scan to find nearest slot.
 */

public class SlotMinHeap {

    // Index 0 is left unused so parent/child calculations stay simple:
    // parent = i / 2, left child = 2 * i, right child = 2 * i + 1.
    private ParkingSlot[] heap;
    private int size;

    // Start with spare capacity so inserts do not resize immediately.
    public SlotMinHeap() { 
        heap = new ParkingSlot[64]; size = 0; 
    }

    /**
     * Insert a slot at the end of the array, then bubble it upward until
     * the min-heap property is restored.
     */
    public void insert(ParkingSlot slot) {
        if (size == heap.length - 1) 
            grow();

        heap[++size] = slot;
        bubbleUp(size);
    }

    /**
     * Remove and return the nearest slot.
     * The root is replaced with the last element, then sifted down.
     */
    public ParkingSlot pollMin() {
        if (isEmpty()) 
            return null;

        ParkingSlot min = heap[1];
        heap[1] = heap[size--];
        siftDown(1);
        return min;
    }

    // Read the nearest slot without changing heap contents.
    public ParkingSlot peekMin() { 
        return isEmpty() ? null : heap[1]; 
    }

    // True when there are no slots in the heap.
    public boolean isEmpty() { 
        return size == 0; 
    }

    // Current number of stored slots.
    public int getSize() { 
        return size; 
    }

    // Reset the heap to an empty state. 
    public void clear() {
        heap = new ParkingSlot[64];
        size = 0;
    }

    /**
     * Snapshot of the heap in array order for UI display.
     * This is heap order, not sorted order.
     */
    public ParkingSlot[] toArray() {
        ParkingSlot[] arr = new ParkingSlot[size];
        System.arraycopy(heap, 1, arr, 0, size);
        return arr;
    }

    // ── Heap helpers ──────────────────────────────────────────────────────────
    // Move a newly inserted node upward until its parent is smaller.
    private void bubbleUp(int i) {
        while (i > 1 && dist(i) < dist(i / 2)) { 
            swap(i, i / 2); 
            i /= 2; 
        }
    }

    /**
     * Push the root downward until both children are greater or the node
     * reaches a valid position.
     */
    private void siftDown(int i) {
        while (2 * i <= size) {
            int child = 2 * i;
            if (child < size && dist(child + 1) < dist(child)) 
                child++;

            if (dist(i) <= dist(child)) 
                break;

            swap(i, child);
            i = child;
        }
    }

    private int dist(int i) { 
        return heap[i].getDistanceToGate(); 
    }

    private void swap(int a, int b) { 
        ParkingSlot t = heap[a]; 
        heap[a] = heap[b]; 
        heap[b] = t; 
    }

    // Double the backing array when it is full.
    private void grow() {
        ParkingSlot[] g = new ParkingSlot[heap.length * 2];
        System.arraycopy(heap, 0, g, 0, heap.length);
        heap = g;
    }
}

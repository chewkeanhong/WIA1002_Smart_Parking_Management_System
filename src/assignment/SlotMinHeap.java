package assignment;

import models.ParkingSlot;

/**
 * Binary Min-Heap ordered by distanceToGate (1-indexed array).
 * Insert: O(log n)  |  Poll min: O(log n)  |  Peek: O(1)
 * Contrast with linear search: O(n) scan to find nearest slot.
 */
public class SlotMinHeap {

    private ParkingSlot[] heap;
    private int size;

    public SlotMinHeap() { heap = new ParkingSlot[64]; size = 0; }

    /** Insert a slot and restore heap property upward — O(log n). */
    public void insert(ParkingSlot slot) {
        if (size == heap.length - 1) grow();
        heap[++size] = slot;
        bubbleUp(size);
    }

    /** Remove and return the nearest (min distance) slot — O(log n). */
    public ParkingSlot pollMin() {
        if (isEmpty()) return null;
        ParkingSlot min = heap[1];
        heap[1] = heap[size--];
        siftDown(1);
        return min;
    }

    public ParkingSlot peekMin() { return isEmpty() ? null : heap[1]; }
    public boolean isEmpty()     { return size == 0; }
    public int     getSize()     { return size; }

    public void clear() {
        heap = new ParkingSlot[64];
        size = 0;
    }

    /** Snapshot of current heap array in heap-index order (for visualisation). */
    public ParkingSlot[] toArray() {
        ParkingSlot[] arr = new ParkingSlot[size];
        System.arraycopy(heap, 1, arr, 0, size);
        return arr;
    }

    // ── Heap helpers ──────────────────────────────────────────────────────────
    private void bubbleUp(int i) {
        while (i > 1 && dist(i) < dist(i / 2)) { swap(i, i / 2); i /= 2; }
    }

    private void siftDown(int i) {
        while (2 * i <= size) {
            int child = 2 * i;
            if (child < size && dist(child + 1) < dist(child)) child++;
            if (dist(i) <= dist(child)) break;
            swap(i, child);
            i = child;
        }
    }

    private int  dist(int i)        { return heap[i].getDistanceToGate(); }
    private void swap(int a, int b) { ParkingSlot t = heap[a]; heap[a] = heap[b]; heap[b] = t; }

    private void grow() {
        ParkingSlot[] g = new ParkingSlot[heap.length * 2];
        System.arraycopy(heap, 0, g, 0, heap.length);
        heap = g;
    }
}

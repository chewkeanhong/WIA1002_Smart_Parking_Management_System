package assignment;

import models.ParkingSlot;

// Min-heap of parking slots, ordered by distance to the gate so the
// nearest free slot is always at the root. We use a 1-indexed array
// (index 0 stays empty) because the parent/child math is cleaner that way.
public class SlotMinHeap {

    // parent of i = i/2, children = 2i and 2i+1
    private ParkingSlot[] heap;
    private int size;

    public SlotMinHeap() {
        heap = new ParkingSlot[64]; size = 0;
    }

    // add the slot at the end, then let it bubble up to its spot
    public void insert(ParkingSlot slot) {
        if (size == heap.length - 1)
            grow();

        heap[++size] = slot;
        bubbleUp(size);
    }

    // take out the nearest slot (the root). move the last element up
    // to the top and sift it back down to fix the order.
    public ParkingSlot pollMin() {
        if (isEmpty())
            return null;

        ParkingSlot min = heap[1];
        heap[1] = heap[size--];
        siftDown(1);
        return min;
    }

    public ParkingSlot peekMin() {
        return isEmpty() ? null : heap[1];
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int getSize() {
        return size;
    }

    public void clear() {
        heap = new ParkingSlot[64];
        size = 0;
    }

    // copy of the array for the UI table. note this is heap order, not sorted.
    public ParkingSlot[] toArray() {
        ParkingSlot[] arr = new ParkingSlot[size];
        System.arraycopy(heap, 1, arr, 0, size);
        return arr;
    }

    // keep swapping with the parent while we're smaller than it
    private void bubbleUp(int i) {
        while (i > 1 && dist(i) < dist(i / 2)) {
            swap(i, i / 2);
            i /= 2;
        }
    }

    // push a node down until both its children are bigger (or it has none)
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

    // double the array size once it fills up
    private void grow() {
        ParkingSlot[] g = new ParkingSlot[heap.length * 2];
        System.arraycopy(heap, 0, g, 0, heap.length);
        heap = g;
    }
}

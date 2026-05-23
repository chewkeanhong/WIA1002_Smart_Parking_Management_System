package gate_control;

import models.Vehicle;

/**
 * Custom FIFO queue backed by a singly-linked list.
 * Enqueue: O(1)  |  Dequeue: O(1)  |  Peek: O(1)
 */
public class EntryQueue {

    private static class Node {
        Vehicle vehicle;
        Node next;
        Node(Vehicle v) { this.vehicle = v; }
    }

    private Node front;
    private Node rear;
    private int size;

    public EntryQueue() { front = null; rear = null; size = 0; }

    /** Add vehicle to the back of the queue — O(1). */
    public void enqueue(Vehicle vehicle) {
        Node n = new Node(vehicle);
        if (rear == null) { front = n; rear = n; }
        else { rear.next = n; rear = n; }
        size++;
    }

    /** Remove and return the front vehicle — O(1). */
    public Vehicle dequeue() {
        if (isEmpty()) return null;
        Vehicle v = front.vehicle;
        front = front.next;
        if (front == null) rear = null;
        size--;
        return v;
    }

    public Vehicle peek()    { return isEmpty() ? null : front.vehicle; }
    public boolean isEmpty() { return size == 0; }
    public int getSize()     { return size; }

    /** Add vehicle to the FRONT of the queue (used by undo of a PROCESSED action) — O(1). */
    public void enqueueAtFront(Vehicle vehicle) {
        Node n = new Node(vehicle);
        if (front == null) { front = n; rear = n; }
        else { n.next = front; front = n; }
        size++;
    }

    /** Remove the LAST (most recently enqueued) vehicle (used by undo of an ENQUEUED action) — O(n). */
    public Vehicle removeLast() {
        if (isEmpty()) return null;
        if (front == rear) {          // only one node
            Vehicle v = front.vehicle;
            front = null; rear = null; size--;
            return v;
        }
        Node curr = front;
        while (curr.next != rear) curr = curr.next;
        Vehicle v = rear.vehicle;
        curr.next = null; rear = curr; size--;
        return v;
    }

    /**
     * Removes the first node holding the given vehicle (by reference). Returns true if found.
     * Used when a user cancels a queued entry via the UI before it is dequeued.
     */
    public boolean remove(Vehicle vehicle) {
        if (isEmpty() || vehicle == null) return false;
        if (front.vehicle == vehicle) {
            front = front.next;
            if (front == null) rear = null;
            size--;
            return true;
        }
        Node curr = front;
        while (curr.next != null && curr.next.vehicle != vehicle) curr = curr.next;
        if (curr.next == null) return false;
        if (curr.next == rear) rear = curr;
        curr.next = curr.next.next;
        size--;
        return true;
    }

    /** Returns a snapshot array in FIFO order for UI display. */
    public Vehicle[] toArray() {
        Vehicle[] arr = new Vehicle[size];
        Node curr = front;
        for (int i = 0; i < size; i++) { arr[i] = curr.vehicle; curr = curr.next; }
        return arr;
    }
}

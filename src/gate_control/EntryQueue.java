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

    /** Returns a snapshot array in FIFO order for UI display. */
    public Vehicle[] toArray() {
        Vehicle[] arr = new Vehicle[size];
        Node curr = front;
        for (int i = 0; i < size; i++) { arr[i] = curr.vehicle; curr = curr.next; }
        return arr;
    }
}

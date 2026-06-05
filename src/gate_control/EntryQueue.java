package gate_control;

import models.Vehicle;

// Our own FIFO queue for vehicles waiting at the gate.
// Backed by a singly-linked list with front and rear pointers.
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

    // add to the back of the line
    public void enqueue(Vehicle vehicle) {
        Node n = new Node(vehicle);
        if (rear == null) { front = n; rear = n; }
        else { rear.next = n; rear = n; }
        size++;
    }

    // take the vehicle at the front
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

    // put a vehicle back at the front - used when we undo a "processed" action
    public void enqueueAtFront(Vehicle vehicle) {
        Node n = new Node(vehicle);
        if (front == null) { front = n; rear = n; }
        else { n.next = front; front = n; }
        size++;
    }

    // drop the last vehicle we added - used when we undo an "enqueued" action.
    // have to walk the whole list since it's singly linked.
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

    // remove a specific vehicle from anywhere in the queue (by reference).
    // happens when a user cancels their entry before being processed.
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

    // array copy in FIFO order so the UI can list who's waiting
    public Vehicle[] toArray() {
        Vehicle[] arr = new Vehicle[size];
        Node curr = front;
        for (int i = 0; i < size; i++) { arr[i] = curr.vehicle; curr = curr.next; }
        return arr;
    }
}

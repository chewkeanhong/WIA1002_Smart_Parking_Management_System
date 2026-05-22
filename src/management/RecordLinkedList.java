package management;

/**
 * A custom generic linked list to dynamically store records.
 * This satisfies the requirement for dynamic data handling without the 
 * fixed-size limitations of standard arrays.
 */
public class RecordLinkedList<T> {
    
    private Node<T> head;
    private Node<T> tail;
    private int size;

    // Internal Node class
    private static class Node<T> {
        T data;
        Node<T> next;

        Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    public RecordLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Adds a new record to the end of the list.
     * Time Complexity: O(1) - Because we maintain a tail pointer, 
     * insertion at the end is constant time.
     */
    public void add(T item) {
        Node<T> newNode = new Node<>(item);
        if (head == null) {
            head = newNode;
            tail = newNode;
        } else {
            tail.next = newNode;
            tail = newNode;
        }
        size++;
    }

    /**
     * Removes a specific record from the list.
     * Time Complexity: O(N) - In the worst case, we must traverse 
     * the entire list to find the element to remove.
     */
    public boolean remove(T item) {
        if (head == null) return false;

        // If the item to remove is the head
        if (head.data.equals(item)) {
            head = head.next;
            if (head == null) {
                tail = null; // List became empty
            }
            size--;
            return true;
        }

        // Search for the item
        Node<T> current = head;
        while (current.next != null) {
            if (current.next.data.equals(item)) {
                current.next = current.next.next;
                if (current.next == null) {
                    tail = current; // We removed the tail node
                }
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /**
     * Displays all records in the list to standard output.
     * Time Complexity: O(N) - Requires iterating through every node.
     */
    public void display() {
        if (head == null) {
            System.out.println("No records found.");
            return;
        }
        Node<T> current = head;
        while (current != null) {
            System.out.println(current.data.toString());
            current = current.next;
        }
    }

    /**
     * Returns the current size of the list.
     * Time Complexity: O(1)
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns a java.util.List snapshot for UI consumption.
     * Time Complexity: O(N)
     */
    public java.util.List<T> toList() {
        java.util.List<T> list = new java.util.ArrayList<>();
        Node<T> curr = head;
        while (curr != null) { list.add(curr.data); curr = curr.next; }
        return list;
    }
}
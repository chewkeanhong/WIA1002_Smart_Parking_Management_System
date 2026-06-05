package management;

// Our own generic linked list for storing records. We wrote this instead
// of using an array so it can grow as needed without a fixed size.
public class RecordLinkedList<T> {

    private Node<T> head;
    private Node<T> tail;
    private int size;

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

    // add to the end. we keep a tail pointer so this stays cheap.
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

    // remove an item - might have to walk the whole list to find it
    public boolean remove(T item) {
        if (head == null) return false;

        // special case: it's the head node
        if (head.data.equals(item)) {
            head = head.next;
            if (head == null) {
                tail = null; // list is now empty
            }
            size--;
            return true;
        }

        Node<T> current = head;
        while (current.next != null) {
            if (current.next.data.equals(item)) {
                current.next = current.next.next;
                if (current.next == null) {
                    tail = current; // we just removed the tail
                }
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    // print everything out (handy for debugging in the console)
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

    public int getSize() {
        return size;
    }

    // dump everything into a normal List so the UI tables can use it
    public java.util.List<T> toList() {
        java.util.List<T> list = new java.util.ArrayList<>();
        Node<T> curr = head;
        while (curr != null) { list.add(curr.data); curr = curr.next; }
        return list;
    }
}
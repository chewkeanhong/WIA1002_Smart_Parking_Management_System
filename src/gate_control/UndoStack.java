package gate_control;

import models.Vehicle;

// LIFO stack that remembers gate actions so we can undo them.
// Backed by a plain array that doubles when it runs out of room.
public class UndoStack {

    public static class Action {
        public final String  type;    // "ENQUEUED" | "PROCESSED" | "EXITED"
        public final Vehicle vehicle;

        public Action(String type, Vehicle vehicle) {
            this.type    = type;
            this.vehicle = vehicle;
        }

        @Override
        public String toString() {
            return type + ": " + vehicle.getLicensePlate() + " (" + vehicle.getOwnerName() + ")";
        }
    }

    private Action[] stack;
    private int top;

    public UndoStack() { stack = new Action[32]; top = -1; }

    public void push(Action action) {
        if (top == stack.length - 1) {     // full, so grow it first
            Action[] grown = new Action[stack.length * 2];
            System.arraycopy(stack, 0, grown, 0, stack.length);
            stack = grown;
        }
        stack[++top] = action;
    }

    public Action pop()  { return isEmpty() ? null : stack[top--]; }

    public Action  peek()    { return isEmpty() ? null : stack[top]; }
    public boolean isEmpty() { return top < 0; }
    public int     getSize() { return top + 1; }

    // wipe out every action for a vehicle so a later Undo can't bring it back.
    // returns how many we removed. compacts the array in place.
    public int removeActionsFor(Vehicle vehicle) {
        if (vehicle == null || isEmpty()) return 0;
        int write = 0, removed = 0;
        for (int read = 0; read <= top; read++) {
            if (stack[read].vehicle == vehicle) { removed++; continue; }
            stack[write++] = stack[read];
        }
        for (int i = write; i <= top; i++) stack[i] = null;
        top = write - 1;
        return removed;
    }

    // copy in push order (bottom to top) for the UI list
    public Action[] toArray() {
        Action[] arr = new Action[top + 1];
        System.arraycopy(stack, 0, arr, 0, top + 1);
        return arr;
    }
}

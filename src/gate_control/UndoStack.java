package gate_control;

import models.Vehicle;

/**
 * LIFO stack backed by a resizable array.
 * Push: O(1) amortised  |  Pop: O(1)  |  Peek: O(1)
 */
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

    /** Push an action — O(1) amortised. */
    public void push(Action action) {
        if (top == stack.length - 1) {
            Action[] grown = new Action[stack.length * 2];
            System.arraycopy(stack, 0, grown, 0, stack.length);
            stack = grown;
        }
        stack[++top] = action;
    }

    /** Pop the most recent action — O(1). */
    public Action pop()  { return isEmpty() ? null : stack[top--]; }

    public Action  peek()    { return isEmpty() ? null : stack[top]; }
    public boolean isEmpty() { return top < 0; }
    public int     getSize() { return top + 1; }

    /**
     * Removes every action that references the given vehicle. Returns the number of actions removed.
     * Used when a user cancels an entry via the UI so a later Undo can't resurrect it.
     */
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

    /** Returns a snapshot in push order (bottom → top) for UI display. */
    public Action[] toArray() {
        Action[] arr = new Action[top + 1];
        System.arraycopy(stack, 0, arr, 0, top + 1);
        return arr;
    }
}

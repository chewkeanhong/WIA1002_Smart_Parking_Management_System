package gate_control;

import models.Vehicle;

/**
 * Orchestrates the gate: incoming vehicles are queued (FIFO),
 * every mutation is recorded on the undo stack (LIFO).
 */
public class GateProcessor {

    private final EntryQueue entryQueue;
    private final UndoStack  undoStack;

    public GateProcessor() {
        entryQueue = new EntryQueue();
        undoStack  = new UndoStack();
    }

    /** Vehicle arrives at the gate → enqueue. */
    public void vehicleArrives(Vehicle v) {
        entryQueue.enqueue(v);
        undoStack.push(new UndoStack.Action("ENQUEUED", v));
    }

    /** Process next vehicle in line → dequeue. Returns null if queue empty. */
    public Vehicle processNext() {
        Vehicle v = entryQueue.dequeue();
        if (v != null) undoStack.push(new UndoStack.Action("PROCESSED", v));
        return v;
    }

    /** Vehicle exits the lot manually. */
    public void vehicleExits(Vehicle v) {
        undoStack.push(new UndoStack.Action("EXITED", v));
    }

    /**
     * Undo the most recent recorded action AND reverse its effect on the queue.
     * ENQUEUED → remove that vehicle from the back of the queue.
     * PROCESSED → put that vehicle back at the front of the queue.
     */
    public UndoStack.Action undoLast() {
        UndoStack.Action action = undoStack.pop();
        if (action == null) return null;

        if ("ENQUEUED".equals(action.type)) {
            entryQueue.removeLast();          // remove from back — O(n)
        } else if ("PROCESSED".equals(action.type)) {
            entryQueue.enqueueAtFront(action.vehicle); // restore to front — O(1)
        }
        return action;
    }

    /** Returns true if a PROCESSED action for this plate exists in the undo stack. */
    public boolean wasApproved(String licensePlate) {
        for (UndoStack.Action a : undoStack.toArray()) {
            if ("PROCESSED".equals(a.type) && a.vehicle.getLicensePlate().equals(licensePlate)) {
                return true;
            }
        }
        return false;
    }

    public EntryQueue getEntryQueue() { return entryQueue; }
    public UndoStack  getUndoStack()  { return undoStack;  }
}

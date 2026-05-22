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

    /** Undo the most recent recorded action. */
    public UndoStack.Action undoLast() { return undoStack.pop(); }

    public EntryQueue getEntryQueue() { return entryQueue; }
    public UndoStack  getUndoStack()  { return undoStack;  }
}

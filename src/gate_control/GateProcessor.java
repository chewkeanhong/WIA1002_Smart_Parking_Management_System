package gate_control;

import models.Vehicle;

// Ties the queue and the undo stack together. Every time a vehicle
// moves through the gate we both update the queue and log the action
// so it can be undone later.
public class GateProcessor {

    private final EntryQueue entryQueue;
    private final UndoStack  undoStack;

    public GateProcessor() {
        entryQueue = new EntryQueue();
        undoStack  = new UndoStack();
    }

    // car shows up at the gate -> join the queue
    public void vehicleArrives(Vehicle v) {
        entryQueue.enqueue(v);
        undoStack.push(new UndoStack.Action("ENQUEUED", v));
    }

    // let the next car through. null if nobody's waiting.
    public Vehicle processNext() {
        Vehicle v = entryQueue.dequeue();
        if (v != null) undoStack.push(new UndoStack.Action("PROCESSED", v));
        return v;
    }

    public void vehicleExits(Vehicle v) {
        undoStack.push(new UndoStack.Action("EXITED", v));
    }

    // undo the last thing that happened and actually reverse it in the queue:
    //   ENQUEUED  -> take the car back off the end of the queue
    //   PROCESSED -> put the car back at the front
    public UndoStack.Action undoLast() {
        UndoStack.Action action = undoStack.pop();
        if (action == null) return null;

        if ("ENQUEUED".equals(action.type)) {
            entryQueue.removeLast();
        } else if ("PROCESSED".equals(action.type)) {
            entryQueue.enqueueAtFront(action.vehicle);
        }
        return action;
    }

    // true if this plate was already processed (approved) at some point
    public boolean wasApproved(String licensePlate) {
        String normalizedPlate = Vehicle.normalizePlate(licensePlate);
        for (UndoStack.Action a : undoStack.toArray()) {
            if ("PROCESSED".equals(a.type)
                    && Vehicle.normalizePlate(a.vehicle.getLicensePlate()).equals(normalizedPlate)) {
                return true;
            }
        }
        return false;
    }

    public EntryQueue getEntryQueue() { return entryQueue; }
    public UndoStack  getUndoStack()  { return undoStack;  }

    public void clearAll() {
        while (entryQueue.dequeue() != null) { /* drain queue */ }
        while (undoStack.pop()      != null) { /* drain stack */ }
    }

    // completely remove a vehicle from the gate: pull it out of the queue
    // and delete any of its actions from the undo stack. used when a user
    // cancels and we want it gone for good.
    public void purgeVehicle(Vehicle v) {
        if (v == null) return;
        entryQueue.remove(v);
        undoStack.removeActionsFor(v);
    }
}

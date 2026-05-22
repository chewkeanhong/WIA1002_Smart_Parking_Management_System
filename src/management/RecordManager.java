package management;

import models.ParkingSlot;
import models.Vehicle;

/**
 * Manages the dynamic storage and retrieval of Vehicle and ParkingSlot records.
 * Acts as the logic layer for the UI and Core application.
 */
public class RecordManager {

    private RecordLinkedList<Vehicle> vehicleRecords;
    private RecordLinkedList<ParkingSlot> parkingSlotRecords;

    public RecordManager() {
        this.vehicleRecords = new RecordLinkedList<>();
        this.parkingSlotRecords = new RecordLinkedList<>();
    }

    // ==========================================
    // VEHICLE MANAGEMENT
    // ==========================================

    public void addVehicleRecord(Vehicle vehicle) {
        if (vehicle != null) {
            vehicleRecords.add(vehicle);
            System.out.println("Vehicle added: " + vehicle.getLicensePlate());
        }
    }

    public boolean removeVehicleRecord(Vehicle vehicle) {
        boolean success = vehicleRecords.remove(vehicle);
        if (success) {
            System.out.println("Vehicle removed: " + vehicle.getLicensePlate());
        } else {
            System.out.println("Vehicle not found: " + vehicle.getLicensePlate());
        }
        return success;
    }

    public void displayAllVehicles() {
        System.out.println("--- Vehicle Records ---");
        vehicleRecords.display();
        System.out.println("Total Vehicles: " + vehicleRecords.getSize());
    }

    // ==========================================
    // PARKING SLOT MANAGEMENT
    // ==========================================

    public void addParkingSlotRecord(ParkingSlot slot) {
        if (slot != null) {
            parkingSlotRecords.add(slot);
            System.out.println("Parking Slot added: " + slot.getSlotId());
        }
    }

    public boolean removeParkingSlotRecord(ParkingSlot slot) {
        boolean success = parkingSlotRecords.remove(slot);
        if (success) {
            System.out.println("Parking Slot removed: " + slot.getSlotId());
        } else {
            System.out.println("Parking Slot not found: " + slot.getSlotId());
        }
        return success;
    }

    public void displayAllParkingSlots() {
        System.out.println("--- Parking Slot Records ---");
        parkingSlotRecords.display();
        System.out.println("Total Slots: " + parkingSlotRecords.getSize());
    }
}
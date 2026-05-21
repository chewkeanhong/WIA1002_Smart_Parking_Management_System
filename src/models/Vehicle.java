package models;

public class Vehicle {


    private String licensePlate;
    private String ownerName;
    private long entryTime;
    private String assignedSlotId;


    public Vehicle(String licensePlate, String ownerName, long entryTime) {
        this.licensePlate = licensePlate;
        this.ownerName = ownerName;
        this.entryTime = entryTime;
    }


    public String getLicensePlate() {
        return licensePlate;
    }


    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }


    public String getOwnerName() {
        return ownerName;
    }


    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }


    public long getEntryTime() {
        return entryTime;
    }


    public void setEntryTime(long entryTime) {
        this.entryTime = entryTime;
    }


    public String getAssignedSlotId() {
        return assignedSlotId;
    }


    public void setAssignedSlotId(String assignedSlotId) {
        this.assignedSlotId = assignedSlotId;
    }


    public String toString() {
        return "Vehicle: " + licensePlate + " | Owner: " + ownerName + " | Entry Time: " + entryTime
                + " | Assigned Slot: " + assignedSlotId;
    }


   
   
}



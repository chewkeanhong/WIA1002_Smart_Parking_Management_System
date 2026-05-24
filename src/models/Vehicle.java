package models;

public class Vehicle {

    public static String normalizePlate(String plate) {
        if (plate == null) {
            return null;
        }
        return plate.replaceAll("\\s+", "").toUpperCase();
    }


    private String licensePlate;
    private String ownerName;
    private long entryTime;
    private String assignedSlotId;
    private String preferredGateId;


    public Vehicle(String licensePlate, String ownerName, long entryTime) {
        this.licensePlate = normalizePlate(licensePlate);
        this.ownerName = ownerName;
        this.entryTime = entryTime;
    }


    public Vehicle(String licensePlate, String ownerName, long entryTime, String preferredGateId) {
        this(licensePlate, ownerName, entryTime);
        this.preferredGateId = preferredGateId;
    }


    public String getLicensePlate() {
        return licensePlate;
    }


    public void setLicensePlate(String licensePlate) {
        this.licensePlate = normalizePlate(licensePlate);
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


    public String getPreferredGateId() {
        return preferredGateId;
    }


    public void setPreferredGateId(String preferredGateId) {
        this.preferredGateId = preferredGateId;
    }


    public String toString() {
        return "Vehicle: " + licensePlate + " | Owner: " + ownerName + " | Entry Time: " + entryTime
                + " | Assigned Slot: " + assignedSlotId
                + (preferredGateId == null ? "" : " | Preferred Gate: " + preferredGateId);
    }


   
   
}



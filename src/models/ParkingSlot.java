package models;


public class ParkingSlot {
    private String slotId;
    private boolean isOccupied;
    private int distanceToGate;
    private Vehicle parkedVehicle;


    public ParkingSlot(String slotId, int distanceToGate) {
        this.slotId = slotId;
        this.distanceToGate = distanceToGate;
        this.isOccupied = false;
        this.parkedVehicle = null;
    }


    public String getSlotId() {
        return slotId;
    }


    public void setSlotId(String slotId) {
        this.slotId = slotId;
    }


    public boolean isOccupied() {
        return isOccupied;
    }


    public void setOccupied(boolean occupied) {
        isOccupied = occupied;
    }


    public int getDistanceToGate() {
        return distanceToGate;
    }


    public void setDistanceToGate(int distanceToGate) {
        this.distanceToGate = distanceToGate;
    }


    public Vehicle getParkedVehicle() {
        return parkedVehicle;
    }


    public void setParkedVehicle(Vehicle parkedVehicle) {
        this.parkedVehicle = parkedVehicle;
        this.isOccupied = (parkedVehicle != null);
    }


    @Override
    public String toString() {
        return "Slot ID: " + slotId + " | Distance to Gate: " + distanceToGate +
              " | Occupied: " + isOccupied +  (isOccupied ? " | Vehicle: " + parkedVehicle.getLicensePlate() : "");
    }


}

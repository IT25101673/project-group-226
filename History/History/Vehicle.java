package com.parking.system;

public class Vehicle {
    private String vehicleNumber; // Private variables (Encapsulation)
    private String ownerName;

    public Vehicle(String vehicleNumber, String ownerName) {
        this.vehicleNumber = vehicleNumber;
        this.ownerName = ownerName;
    }

    // Getters and Setters
    public String getVehicleNumber() {
        return vehicleNumber;
    }
    public String getOwnerName() {
        return ownerName;
    }
}
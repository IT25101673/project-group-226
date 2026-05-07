package com.parking.system;

// Inheritance
public class Car extends Vehicle {
    private String model;

    public Car(String vehicleNumber, String ownerName, String model) {
        super(vehicleNumber, ownerName); // Parent constructor
        this.model = model;
    }

    public String getModel() {
        return model;
    }
}
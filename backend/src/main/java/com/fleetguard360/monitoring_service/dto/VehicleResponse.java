package com.fleetguard360.monitoring_service.dto;

import com.fleetguard360.monitoring_service.model.FuelType;
import com.fleetguard360.monitoring_service.model.Vehicle;
import com.fleetguard360.monitoring_service.model.VehicleStatus;

import java.time.LocalDateTime;

/**
 * DTO para respuesta de operaciones con vehículos
 */
public class VehicleResponse {

    private Long id;
    private String licensePlate;
    private String model;
    private String brand;
    private Integer year;
    private Integer capacity;
    private VehicleStatus status;
    private String statusDisplayName;
    private FuelType fuelType;
    private String fuelTypeDisplayName;
    private Integer mileage;
    private String color;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Constructors
    public VehicleResponse() {}

    public VehicleResponse(Vehicle vehicle) {
        this.id = vehicle.getId();
        this.licensePlate = vehicle.getLicensePlate();
        this.model = vehicle.getModel();
        this.brand = vehicle.getBrand();
        this.year = vehicle.getYear();
        this.capacity = vehicle.getCapacity();
        this.status = vehicle.getStatus();
        this.statusDisplayName = vehicle.getStatus() != null ? vehicle.getStatus().getDisplayName() : null;
        this.fuelType = vehicle.getFuelType();
        this.fuelTypeDisplayName = vehicle.getFuelType() != null ? vehicle.getFuelType().getDisplayName() : null;
        this.mileage = vehicle.getMileage();
        this.color = vehicle.getColor();
        this.notes = vehicle.getNotes();
        this.createdAt = vehicle.getCreatedAt();
        this.updatedAt = vehicle.getUpdatedAt();
        this.createdBy = vehicle.getCreatedBy();
        this.updatedBy = vehicle.getUpdatedBy();
    }

    // Static factory method
    public static VehicleResponse from(Vehicle vehicle) {
        return new VehicleResponse(vehicle);
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public VehicleStatus getStatus() {
        return status;
    }

    public void setStatus(VehicleStatus status) {
        this.status = status;
    }

    public String getStatusDisplayName() {
        return statusDisplayName;
    }

    public void setStatusDisplayName(String statusDisplayName) {
        this.statusDisplayName = statusDisplayName;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public String getFuelTypeDisplayName() {
        return fuelTypeDisplayName;
    }

    public void setFuelTypeDisplayName(String fuelTypeDisplayName) {
        this.fuelTypeDisplayName = fuelTypeDisplayName;
    }

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    @Override
    public String toString() {
        return "VehicleResponse{" +
                "id=" + id +
                ", licensePlate='" + licensePlate + '\'' +
                ", model='" + model + '\'' +
                ", brand='" + brand + '\'' +
                ", capacity=" + capacity +
                ", status=" + status +
                '}';
    }
}
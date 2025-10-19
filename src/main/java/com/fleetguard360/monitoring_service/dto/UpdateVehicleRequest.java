package com.fleetguard360.monitoring_service.dto;

import com.fleetguard360.monitoring_service.model.FuelType;
import com.fleetguard360.monitoring_service.model.VehicleStatus;
import jakarta.validation.constraints.*;

/**
 * DTO para actualizar un vehículo existente
 */
public class UpdateVehicleRequest {

    @NotBlank(message = "La placa es obligatoria")
    @Pattern(regexp = "^[A-Z]{3}-?[0-9]{3}$", 
             message = "Formato de placa inválido (ej: ABC-123 o ABC123)")
    private String licensePlate;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(min = 2, max = 50, message = "El modelo debe tener entre 2 y 50 caracteres")
    private String model;

    @Size(max = 30, message = "La marca no puede exceder 30 caracteres")
    private String brand;

    @Min(value = 1980, message = "El año debe ser mayor a 1980")
    @Max(value = 2030, message = "El año no puede ser mayor a 2030")
    private Integer year;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    @Max(value = 100, message = "La capacidad no puede exceder 100 personas")
    private Integer capacity;

    @NotNull(message = "El estado es obligatorio")
    private VehicleStatus status;

    private FuelType fuelType;

    @Min(value = 0, message = "El kilometraje no puede ser negativo")
    private Integer mileage;

    @Size(max = 20, message = "El color no puede exceder 20 caracteres")
    private String color;

    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;

    // Constructors
    public UpdateVehicleRequest() {}

    // Getters and Setters
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

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
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

    @Override
    public String toString() {
        return "UpdateVehicleRequest{" +
                "licensePlate='" + licensePlate + '\'' +
                ", model='" + model + '\'' +
                ", brand='" + brand + '\'' +
                ", capacity=" + capacity +
                ", status=" + status +
                '}';
    }
}
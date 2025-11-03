package com.fleetguard360.monitoring_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entidad que representa un vehículo de la flota
 */
@Entity
@Table(name = "vehicles")
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "license_plate", unique = true, nullable = false, length = 10)
    @NotBlank(message = "La placa es obligatoria")
    @Pattern(regexp = "^[A-Z]{3}-[0-9]{3}$|^[A-Z]{3}[0-9]{3}$", 
             message = "Formato de placa inválido (ej: ABC-123 o ABC123)")
    private String licensePlate;

    @Column(name = "model", nullable = false, length = 50)
    @NotBlank(message = "El modelo es obligatorio")
    @Size(min = 2, max = 50, message = "El modelo debe tener entre 2 y 50 caracteres")
    private String model;

    @Column(name = "brand", length = 30)
    @Size(max = 30, message = "La marca no puede exceder 30 caracteres")
    private String brand;

    @Column(name = "year")
    @Min(value = 1980, message = "El año debe ser mayor a 1980")
    @Max(value = 2030, message = "El año no puede ser mayor a 2030")
    private Integer year;

    @Column(name = "capacity", nullable = false)
    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    @Max(value = 100, message = "La capacidad no puede exceder 100 personas")
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @NotNull(message = "El estado es obligatorio")
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @Column(name = "fuel_type", length = 20)
    @Enumerated(EnumType.STRING)
    private FuelType fuelType;

    @Column(name = "mileage")
    @Min(value = 0, message = "El kilometraje no puede ser negativo")
    private Integer mileage;

    @Column(name = "color", length = 20)
    @Size(max = 20, message = "El color no puede exceder 20 caracteres")
    private String color;

    @Column(name = "notes", length = 500)
    @Size(max = 500, message = "Las notas no pueden exceder 500 caracteres")
    private String notes;

    // Campos de auditoría
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_by", length = 50)
    private String updatedBy;

		@Column(name = "latitude")
		private Double latitude = 6.140661;

		@Column(name = "longitude")
		private Double longitude = -75.379754;

    // Constructors
    public Vehicle() {
        this.createdAt = LocalDateTime.now();
    }

    public Vehicle(String licensePlate, String model, Integer capacity, VehicleStatus status) {
        this();
        this.licensePlate = licensePlate;
        this.model = model;
        this.capacity = capacity;
        this.status = status;
    }

    // Lifecycle callbacks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
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
        this.licensePlate = licensePlate != null ? licensePlate.toUpperCase() : null;
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

    // Business methods
    public boolean isAvailable() {
        return VehicleStatus.AVAILABLE.equals(this.status);
    }

    public boolean isInMaintenance() {
        return VehicleStatus.MAINTENANCE.equals(this.status);
    }

    public boolean isInUse() {
        return VehicleStatus.IN_USE.equals(this.status);
    }

		 

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicle vehicle = (Vehicle) o;
        return Objects.equals(id, vehicle.id) &&
               Objects.equals(licensePlate, vehicle.licensePlate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, licensePlate);
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "id=" + id +
                ", licensePlate='" + licensePlate + '\'' +
                ", model='" + model + '\'' +
                ", brand='" + brand + '\'' +
                ", capacity=" + capacity +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
}
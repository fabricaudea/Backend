package com.fleetguard360.monitoring_service.dto;

import com.fleetguard360.monitoring_service.model.VehicleStatus;
import jakarta.validation.constraints.*;

/**
 * DTO para request de creación de vehículos compatible con el frontend
 */
public class VehicleFormRequest {

    @NotBlank(message = "La placa es obligatoria")
    @Pattern(regexp = "^[A-Z]{3}-?[0-9]{3}$", 
             message = "Formato de placa inválido (ej: ABC-123 o ABC123)")
    private String placa;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(min = 2, max = 100, message = "El modelo debe tener entre 2 y 100 caracteres")
    private String modelo;

    @NotNull(message = "La capacidad es obligatoria")
    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    @Max(value = 100, message = "La capacidad no puede exceder 100 personas")
    private Integer capacidad;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(activo|inactivo|mantenimiento)$", 
             message = "Estado debe ser: activo, inactivo o mantenimiento")
    private String estado;

    // Constructor vacío
    public VehicleFormRequest() {}

    // Constructor con parámetros
    public VehicleFormRequest(String placa, String modelo, Integer capacidad, String estado) {
        this.placa = placa;
        this.modelo = modelo;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    /**
     * Mapea el estado del frontend al enum del backend
     */
    public VehicleStatus mapToVehicleStatus() {
        if (estado == null) return VehicleStatus.INACTIVE;
        
        return switch (estado.toLowerCase()) {
            case "activo" -> VehicleStatus.AVAILABLE;
            case "mantenimiento" -> VehicleStatus.MAINTENANCE;
            case "inactivo" -> VehicleStatus.INACTIVE;
            default -> VehicleStatus.INACTIVE;
        };
    }

    /**
     * Normaliza la placa agregando guión si no lo tiene
     */
    public String getNormalizedPlaca() {
        if (placa == null) return null;
        
        String upperPlaca = placa.toUpperCase().trim();
        if (upperPlaca.length() == 6 && !upperPlaca.contains("-")) {
            return upperPlaca.substring(0, 3) + "-" + upperPlaca.substring(3);
        }
        return upperPlaca;
    }

    // Getters and Setters
    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "VehicleFormRequest{" +
                "placa='" + placa + '\'' +
                ", modelo='" + modelo + '\'' +
                ", capacidad=" + capacidad +
                ", estado='" + estado + '\'' +
                '}';
    }
}
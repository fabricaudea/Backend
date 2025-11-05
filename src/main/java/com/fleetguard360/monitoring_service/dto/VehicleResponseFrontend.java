package com.fleetguard360.monitoring_service.dto;

import com.fleetguard360.monitoring_service.model.Vehicle;
import com.fleetguard360.monitoring_service.model.VehicleStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.format.DateTimeFormatter;

/**
 * DTO para respuesta de vehículos compatible con el frontend
 * Mapea los campos del backend a los nombres esperados por el frontend
 */
public class VehicleResponseFrontend {

    private String id;
    private String placa;
    private String modelo;
    private Integer capacidad;
    private String estado;
    @JsonProperty("fechaCreacion")
    private String fechaCreacion;
    @JsonProperty("fechaActualizacion")
    private String fechaActualizacion;
    @JsonProperty("viajesActivos")
    private Integer viajesActivos;

		// Constantes
		private static final String inactivoString = "inactivo"; 

    // Constructor vacío
    public VehicleResponseFrontend() {}

    // Constructor desde entidad Vehicle
    public VehicleResponseFrontend(Vehicle vehicle) {
        this.id = vehicle.getId().toString();
        this.placa = vehicle.getLicensePlate();
        this.modelo = vehicle.getModel();
        this.capacidad = vehicle.getCapacity();
        this.estado = mapStatusToFrontend(vehicle.getStatus());
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        this.fechaCreacion = vehicle.getCreatedAt() != null ? 
            vehicle.getCreatedAt().toLocalDate().format(formatter) : null;
        this.fechaActualizacion = vehicle.getUpdatedAt() != null ? 
            vehicle.getUpdatedAt().toLocalDate().format(formatter) : null;
            
        // Por ahora inicializamos en 0, después se podría calcular desde trips
        this.viajesActivos = 0;
    }

    /**
     * Mapea los estados del backend a los esperados por el frontend
     */
    private String mapStatusToFrontend(VehicleStatus status) {
        if (status == null) return inactivoString;
        
        return switch (status) {
            case AVAILABLE, IN_USE -> "activo";
            case MAINTENANCE -> "mantenimiento";
            case OUT_OF_SERVICE, INACTIVE -> inactivoString;
            default -> inactivoString;
        };
    }

    // Static factory method
    public static VehicleResponseFrontend from(Vehicle vehicle) {
        return new VehicleResponseFrontend(vehicle);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(String fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    public Integer getViajesActivos() {
        return viajesActivos;
    }

    public void setViajesActivos(Integer viajesActivos) {
        this.viajesActivos = viajesActivos;
    }

    @Override
    public String toString() {
        return "VehicleResponseFrontend{" +
                "id='" + id + '\'' +
                ", placa='" + placa + '\'' +
                ", modelo='" + modelo + '\'' +
                ", capacidad=" + capacidad +
                ", estado='" + estado + '\'' +
                ", fechaCreacion='" + fechaCreacion + '\'' +
                ", fechaActualizacion='" + fechaActualizacion + '\'' +
                ", viajesActivos=" + viajesActivos +
                '}';
    }
}
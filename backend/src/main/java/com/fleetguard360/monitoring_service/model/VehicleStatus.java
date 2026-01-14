package com.fleetguard360.monitoring_service.model;

/**
 * Estados posibles de un vehículo en la flota
 */
public enum VehicleStatus {
    AVAILABLE("Disponible", "Vehículo listo para ser asignado"),
    IN_USE("En Uso", "Vehículo actualmente asignado a un viaje"),
    MAINTENANCE("En Mantenimiento", "Vehículo en reparación o mantenimiento"),
    OUT_OF_SERVICE("Fuera de Servicio", "Vehículo temporalmente no disponible"),
    INACTIVE("Inactivo", "Vehículo dado de baja del servicio");

    private final String displayName;
    private final String description;

    VehicleStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Verifica si el vehículo puede ser asignado a un viaje
     */
    public boolean canBeAssigned() {
        return this == AVAILABLE;
    }

    /**
     * Verifica si el vehículo está activo en el sistema
     */
    public boolean isActive() {
        return this != INACTIVE;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
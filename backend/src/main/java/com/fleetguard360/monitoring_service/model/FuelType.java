package com.fleetguard360.monitoring_service.model;

/**
 * Tipos de combustible para vehículos
 */
public enum FuelType {
    GASOLINE("Gasolina", "Motor a gasolina"),
    DIESEL("Diésel", "Motor diésel"),
    ELECTRIC("Eléctrico", "Motor eléctrico"),
    HYBRID("Híbrido", "Motor híbrido gasolina-eléctrico"),
    GAS("Gas", "Motor a gas natural vehicular (GNV)"),
    ETHANOL("Etanol", "Motor a etanol o biocombustible");

    private final String displayName;
    private final String description;

    FuelType(String displayName, String description) {
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
     * Verifica si es un combustible ecológico
     */
    public boolean isEcoFriendly() {
        return this == ELECTRIC || this == HYBRID || this == ETHANOL;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
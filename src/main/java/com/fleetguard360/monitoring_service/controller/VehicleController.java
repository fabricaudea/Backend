package com.fleetguard360.monitoring_service.controller;

import com.fleetguard360.monitoring_service.model.Vehicle;
import com.fleetguard360.monitoring_service.model.VehicleStatus;
import com.fleetguard360.monitoring_service.service.VehicleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Vehículos - API REST Pública
 * Todos los endpoints son accesibles sin autenticación
 */
@RestController
@RequestMapping("/api/vehicles")
@Tag(name = "Vehículos", description = "Endpoints para gestión de vehículos")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    /**
     * Listar todos los vehículos
     */
    @GetMapping
    @Operation(summary = "Obtener todos los vehículos")
    public ResponseEntity<List<Vehicle>> getAllVehicles() {
        List<Vehicle> vehicles = vehicleService.getAllVehicles();
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Obtener vehículo por ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener vehículo por ID")
    public ResponseEntity<Vehicle> getVehicleById(@PathVariable Long id) {
        Vehicle vehicle = vehicleService.getVehicleById(id);
        return ResponseEntity.ok(vehicle);
    }

    /**
     * Crear nuevo vehículo
     */
    @PostMapping
    @Operation(summary = "Crear nuevo vehículo")
    public ResponseEntity<Vehicle> createVehicle(@Valid @RequestBody Vehicle vehicle) {
        Vehicle createdVehicle = vehicleService.createVehicle(vehicle);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdVehicle);
    }

    /**
     * Actualizar vehículo existente
     */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar vehículo existente")
    public ResponseEntity<Vehicle> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody Vehicle vehicleDetails) {
        Vehicle updatedVehicle = vehicleService.updateVehicle(id, vehicleDetails);
        return ResponseEntity.ok(updatedVehicle);
    }

    /**
     * Eliminar vehículo
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar vehículo")
    public ResponseEntity<Void> deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Buscar vehículo por placa
     */
    @GetMapping("/by-plate/{plate}")
    @Operation(summary = "Buscar vehículo por placa")
    public ResponseEntity<Vehicle> getVehicleByPlate(@PathVariable String plate) {
        Vehicle vehicle = vehicleService.getVehicleByPlate(plate);
        return ResponseEntity.ok(vehicle);
    }

    /**
     * Filtrar vehículos por estado
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Filtrar vehículos por estado")
    public ResponseEntity<List<Vehicle>> getVehiclesByStatus(@PathVariable VehicleStatus status) {
        List<Vehicle> vehicles = vehicleService.getVehiclesByStatus(status);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Obtener vehículos disponibles
     */
    @GetMapping("/available")
    @Operation(summary = "Obtener vehículos disponibles")
    public ResponseEntity<List<Vehicle>> getAvailableVehicles() {
        List<Vehicle> vehicles = vehicleService.getAvailableVehicles();
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Cambiar estado de vehículo
     */
    @PatchMapping("/{id}/status")
    @Operation(summary = "Cambiar estado de vehículo")
    public ResponseEntity<Vehicle> updateVehicleStatus(
            @PathVariable Long id,
            @RequestParam VehicleStatus status) {
        Vehicle updatedVehicle = vehicleService.updateVehicleStatus(id, status);
        return ResponseEntity.ok(updatedVehicle);
    }
}

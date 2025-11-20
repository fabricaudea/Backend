package com.fleetguard360.monitoring_service.service;

import com.fleetguard360.monitoring_service.model.Vehicle;
import com.fleetguard360.monitoring_service.model.VehicleStatus;
import com.fleetguard360.monitoring_service.repository.VehicleRepository;
import com.fleetguard360.monitoring_service.exception.ResourceNotFoundException;


import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Servicio de Vehículos - Simplificado sin lógica de seguridad
 */
@Service
@Transactional
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    /**
     * Obtener todos los vehículos
     */
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }

    /**
     * Obtener vehículo por ID
     */
    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));
    }

    /**
     * Crear nuevo vehículo
     */
    public Vehicle createVehicle(Vehicle vehicle) {
        if (vehicle.getStatus() == null) {
            vehicle.setStatus(VehicleStatus.AVAILABLE);
        }
        return vehicleRepository.save(vehicle);
    }

    /**
     * Actualizar vehículo existente
     */
    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        Vehicle vehicle = getVehicleById(id);

        if (vehicleDetails.getLicensePlate() != null) {
            vehicle.setLicensePlate(vehicleDetails.getLicensePlate());
        }
        if (vehicleDetails.getModel() != null) {
            vehicle.setModel(vehicleDetails.getModel());
        }
        if (vehicleDetails.getCapacity() != null) {
            vehicle.setCapacity(vehicleDetails.getCapacity());
        }
        if (vehicleDetails.getStatus() != null) {
            vehicle.setStatus(vehicleDetails.getStatus());
        }

        return vehicleRepository.save(vehicle);
    }

    /**
     * Eliminar vehículo
     */
    public void deleteVehicle(Long id) {
        Vehicle vehicle = getVehicleById(id);
        vehicleRepository.delete(vehicle);
    }

    /**
     * Buscar vehículo por placa
     */
    public Vehicle getVehicleByPlate(String plate) {
        return vehicleRepository.findByLicensePlate(plate)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con placa: " + plate));
    }

    /**
     * Obtener vehículos por estado
     */
    public List<Vehicle> getVehiclesByStatus(VehicleStatus status) {
        return vehicleRepository.findByStatus(status);
    }

    /**
     * Obtener vehículos disponibles
     */
    public List<Vehicle> getAvailableVehicles() {
        return vehicleRepository.findByStatus(VehicleStatus.AVAILABLE);
    }

    /**
     * Cambiar estado de vehículo
     */
    public Vehicle updateVehicleStatus(Long id, VehicleStatus newStatus) {
        Vehicle vehicle = getVehicleById(id);
        vehicle.setStatus(newStatus);
        return vehicleRepository.save(vehicle);
    }
}

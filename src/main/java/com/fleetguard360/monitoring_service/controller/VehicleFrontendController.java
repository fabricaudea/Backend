package com.fleetguard360.monitoring_service.controller;

import com.fleetguard360.monitoring_service.dto.VehicleFormRequest;
import com.fleetguard360.monitoring_service.dto.VehicleResponseFrontend;
import com.fleetguard360.monitoring_service.model.Vehicle;
import com.fleetguard360.monitoring_service.service.VehicleService;
import com.fleetguard360.monitoring_service.dto.CreateVehicleRequest;
import com.fleetguard360.monitoring_service.dto.UpdateVehicleRequest;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST específicamente diseñado para el frontend React
 * Maneja endpoints con formato compatible con el frontend
 */
@RestController
@RequestMapping("/api/frontend/vehicles")
@CrossOrigin(origins = "*", maxAge = 3600)
public class VehicleFrontendController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleFrontendController.class);

    @Autowired
    private VehicleService vehicleService;

    /**
     * Lista todos los vehículos en formato compatible con el frontend
     * GET /api/frontend/vehicles
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<VehicleResponseFrontend>> getAllVehicles() {
        logger.debug("Frontend - Solicitud para listar todos los vehículos");
        
        List<VehicleResponseFrontend> vehicles = vehicleService.getAllVehicles()
                .stream()
                .map(vehicleResponse -> {
                    // Convertir VehicleResponse a Vehicle y luego a VehicleResponseFrontend
                    Vehicle vehicle = new Vehicle();
                    vehicle.setId(vehicleResponse.getId());
                    vehicle.setLicensePlate(vehicleResponse.getLicensePlate());
                    vehicle.setModel(vehicleResponse.getModel());
                    vehicle.setCapacity(vehicleResponse.getCapacity());
                    vehicle.setStatus(vehicleResponse.getStatus());
                    vehicle.setCreatedAt(vehicleResponse.getCreatedAt());
                    vehicle.setUpdatedAt(vehicleResponse.getUpdatedAt());
                    return VehicleResponseFrontend.from(vehicle);
                })
                .collect(Collectors.toList());
        
        logger.debug("Frontend - Retornando {} vehículos", vehicles.size());
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Obtiene un vehículo específico por ID
     * GET /api/frontend/vehicles/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<VehicleResponseFrontend> getVehicleById(@PathVariable String id) {
        logger.debug("Frontend - Solicitud para obtener vehículo ID: {}", id);
        
        try {
            Long vehicleId = Long.parseLong(id);
            var vehicleResponse = vehicleService.getVehicleById(vehicleId);
            
            // Convertir a formato frontend
            Vehicle vehicle = new Vehicle();
            vehicle.setId(vehicleResponse.getId());
            vehicle.setLicensePlate(vehicleResponse.getLicensePlate());
            vehicle.setModel(vehicleResponse.getModel());
            vehicle.setCapacity(vehicleResponse.getCapacity());
            vehicle.setStatus(vehicleResponse.getStatus());
            vehicle.setCreatedAt(vehicleResponse.getCreatedAt());
            vehicle.setUpdatedAt(vehicleResponse.getUpdatedAt());
            
            return ResponseEntity.ok(VehicleResponseFrontend.from(vehicle));
            
        } catch (NumberFormatException e) {
            logger.warn("Frontend - ID de vehículo inválido: {}", id);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Crea un nuevo vehículo con datos del frontend
     * POST /api/frontend/vehicles
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createVehicle(
            @Valid @RequestBody VehicleFormRequest request,
            BindingResult bindingResult) {
        
				String plate = request.getPlaca();
				if (plate != null){
					plate = plate.replaceAll("[\n\r]", "_");
				}
        logger.info("Frontend - Solicitud para crear nuevo vehículo: placa={}", request.getPlaca());

        // Validar errores de entrada
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            
            logger.warn("Frontend - Errores de validación al crear vehículo: {}", errorMessage);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "VALIDATION_ERROR",
                "message", "Datos de entrada inválidos: " + errorMessage
            ));
        }

        try {
            // Convertir request del frontend al DTO del backend
            CreateVehicleRequest backendRequest = new CreateVehicleRequest();
            backendRequest.setLicensePlate(request.getNormalizedPlaca());
            backendRequest.setModel(request.getModelo());
            backendRequest.setCapacity(request.getCapacidad());
            backendRequest.setStatus(request.mapToVehicleStatus());
            
            var vehicleResponse = vehicleService.createVehicle(backendRequest);
            
            // Convertir respuesta al formato del frontend
            Vehicle vehicle = new Vehicle();
            vehicle.setId(vehicleResponse.getId());
            vehicle.setLicensePlate(vehicleResponse.getLicensePlate());
            vehicle.setModel(vehicleResponse.getModel());
            vehicle.setCapacity(vehicleResponse.getCapacity());
            vehicle.setStatus(vehicleResponse.getStatus());
            vehicle.setCreatedAt(vehicleResponse.getCreatedAt());
            vehicle.setUpdatedAt(vehicleResponse.getUpdatedAt());
            
            VehicleResponseFrontend frontendResponse = VehicleResponseFrontend.from(vehicle);
            
            logger.info("Frontend - Vehículo creado exitosamente: ID={}, placa={}", 
                       frontendResponse.getId(), frontendResponse.getPlaca());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(frontendResponse);
            
        } catch (Exception e) {
            logger.error("Frontend - Error al crear vehículo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", "Error interno del servidor"
            ));
        }
    }

    /**
     * Actualiza un vehículo existente
     * PUT /api/frontend/vehicles/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateVehicle(
            @PathVariable String id,
            @Valid @RequestBody VehicleFormRequest request,
            BindingResult bindingResult) {
        
				if (id != null){
					id = id.replaceAll("[\n\r]", "_");
				}
        logger.info("Frontend - Solicitud para actualizar vehículo ID: {}", id);

        // Validar errores de entrada
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            
            logger.warn("Frontend - Errores de validación al actualizar vehículo: {}", errorMessage);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "VALIDATION_ERROR",
                "message", "Datos de entrada inválidos: " + errorMessage
            ));
        }

        try {
            Long vehicleId = Long.parseLong(id);
            
            // Convertir request del frontend al DTO del backend
            UpdateVehicleRequest backendRequest = new UpdateVehicleRequest();
            backendRequest.setLicensePlate(request.getNormalizedPlaca());
            backendRequest.setModel(request.getModelo());
            backendRequest.setCapacity(request.getCapacidad());
            backendRequest.setStatus(request.mapToVehicleStatus());
            
            var vehicleResponse = vehicleService.updateVehicle(vehicleId, backendRequest);
            
            // Convertir respuesta al formato del frontend
            Vehicle vehicle = new Vehicle();
            vehicle.setId(vehicleResponse.getId());
            vehicle.setLicensePlate(vehicleResponse.getLicensePlate());
            vehicle.setModel(vehicleResponse.getModel());
            vehicle.setCapacity(vehicleResponse.getCapacity());
            vehicle.setStatus(vehicleResponse.getStatus());
            vehicle.setCreatedAt(vehicleResponse.getCreatedAt());
            vehicle.setUpdatedAt(vehicleResponse.getUpdatedAt());
            
            VehicleResponseFrontend frontendResponse = VehicleResponseFrontend.from(vehicle);
            
            logger.info("Frontend - Vehículo actualizado exitosamente: ID={}", frontendResponse.getId());
            
            return ResponseEntity.ok(frontendResponse);
            
        } catch (NumberFormatException e) {
            logger.warn("Frontend - ID de vehículo inválido: {}", id);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "INVALID_ID",
                "message", "ID de vehículo inválido"
            ));
        } catch (Exception e) {
            logger.error("Frontend - Error al actualizar vehículo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", "Error interno del servidor"
            ));
        }
    }

    /**
     * Elimina un vehículo
     * DELETE /api/frontend/vehicles/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteVehicle(@PathVariable String id) {
				if (id != null){
					id = id.replaceAll("[\n\r]", "_");
				}
        logger.info("Frontend - Solicitud para eliminar vehículo ID: {}", id);

        try {
            Long vehicleId = Long.parseLong(id);
            
            vehicleService.deleteVehicle(vehicleId);
            
            logger.info("Frontend - Vehículo eliminado exitosamente: ID={}", vehicleId);
            
            return ResponseEntity.ok(Map.of(
                "message", "Vehículo eliminado exitosamente"
            ));
            
        } catch (NumberFormatException e) {
            logger.warn("Frontend - ID de vehículo inválido: {}", id);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "INVALID_ID",
                "message", "ID de vehículo inválido"
            ));
        } catch (Exception e) {
            logger.error("Frontend - Error al eliminar vehículo: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", "Error interno del servidor"
            ));
        }
    }
}

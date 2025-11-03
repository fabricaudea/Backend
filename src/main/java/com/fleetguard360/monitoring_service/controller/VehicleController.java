package com.fleetguard360.monitoring_service.controller;

import com.fleetguard360.monitoring_service.dto.CreateVehicleRequest;
import com.fleetguard360.monitoring_service.dto.UpdateVehicleRequest;
import com.fleetguard360.monitoring_service.dto.VehicleResponse;
import com.fleetguard360.monitoring_service.model.VehicleStatus;
import com.fleetguard360.monitoring_service.service.VehicleService;

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
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión CRUD de vehículos de la flota
 * Implementa las historias de usuario: dar de alta, editar y dar de baja vehículos
 */
@RestController
@RequestMapping("/api/vehicles")
@CrossOrigin(origins = "*", maxAge = 3600)
public class VehicleController {

    private static final Logger logger = LoggerFactory.getLogger(VehicleController.class);

    @Autowired
    private VehicleService vehicleService;

    /**
     * HU: Dar de alta vehículos en la flota
     * POST /api/vehicles
     * 
     * Criterios de aceptación:
     * 1. Validar datos obligatorios (placa, modelo, capacidad, estado)
     * 2. Evitar duplicados por placa
     * 3. El vehículo debe aparecer en la lista después de registrarse
     */
    @PostMapping
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createVehicle(
            @Valid @RequestBody CreateVehicleRequest request,
            BindingResult bindingResult) {
        
				String licensePlate = request.getLicensePlate();

				licensePlate = licensePlate.replaceAll("[\n\r]", "_");

        logger.info("Solicitud para crear nuevo vehículo: placa={}", licensePlate);

        // Validar errores de entrada
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            
            logger.warn("Errores de validación al crear vehículo: {}", errorMessage);
            return ResponseEntity.badRequest().body(new ErrorResponse(
                "VALIDATION_ERROR", 
                "Datos de entrada inválidos: " + errorMessage
            ));
        }

        try {
            VehicleResponse vehicleResponse = vehicleService.createVehicle(request);
            
            logger.info("Vehículo creado exitosamente: ID={}, placa={}", 
                       vehicleResponse.getId(), vehicleResponse.getLicensePlate());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(vehicleResponse);
            
        } catch (Exception e) {
            logger.error("Error al crear vehículo: {}", e.getMessage());
            // Las excepciones específicas son manejadas por GlobalExceptionHandler
            throw e;
        }
    }

    /**
     * Lista todos los vehículos activos de la flota
     * GET /api/vehicles
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<VehicleResponse>> getAllVehicles() {
        logger.debug("Solicitud para listar todos los vehículos");
        
        List<VehicleResponse> vehicles = vehicleService.getAllVehicles();
        
        logger.debug("Retornando {} vehículos", vehicles.size());
        return ResponseEntity.ok(vehicles);
    }

    /**
     * HU: Editar vehículos de la flota
     * GET /api/vehicles/{id} - Obtiene un vehículo específico para edición
     * 
     * Criterio de aceptación:
     * 3. Mostrar error si el vehículo no existe
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<VehicleResponse> getVehicleById(@PathVariable Long id) {
        logger.debug("Solicitud para obtener vehículo ID: {}", id);
        
        VehicleResponse vehicle = vehicleService.getVehicleById(id);
        
        return ResponseEntity.ok(vehicle);
    }

    /**
     * Busca vehículo por placa
     * GET /api/vehicles/by-plate/{licensePlate}
     */
    @GetMapping("/by-plate/{licensePlate}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<VehicleResponse> getVehicleByLicensePlate(
            @PathVariable String licensePlate) {
        logger.debug("Solicitud para obtener vehículo por placa: {}", licensePlate);
        
        VehicleResponse vehicle = vehicleService.getVehicleByLicensePlate(licensePlate);
        
        return ResponseEntity.ok(vehicle);
    }

    /**
     * HU: Editar vehículos de la flota
     * PUT /api/vehicles/{id} - Actualiza un vehículo existente
     * 
     * Criterios de aceptación:
     * 1. Permitir modificar datos (placa, modelo, estado, capacidad)
     * 2. Reflejar cambios inmediatamente
     * 3. Mostrar error si el vehículo no existe
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateVehicle(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVehicleRequest request,
            BindingResult bindingResult) {
        
        logger.info("Solicitud para actualizar vehículo ID: {}", id);

        // Validar errores de entrada
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            
            logger.warn("Errores de validación al actualizar vehículo ID {}: {}", id, errorMessage);
            return ResponseEntity.badRequest().body(new ErrorResponse(
                "VALIDATION_ERROR", 
                "Datos de entrada inválidos: " + errorMessage
            ));
        }

        try {
            VehicleResponse vehicleResponse = vehicleService.updateVehicle(id, request);
            
            logger.info("Vehículo actualizado exitosamente: ID={}, placa={}", 
                       vehicleResponse.getId(), vehicleResponse.getLicensePlate());
            
            return ResponseEntity.ok(vehicleResponse);
            
        } catch (Exception e) {
            logger.error("Error al actualizar vehículo ID {}: {}", id, e.getMessage());
            // Las excepciones específicas son manejadas por GlobalExceptionHandler
            throw e;
        }
    }

    /**
     * HU: Dar de baja vehículos de la flota
     * DELETE /api/vehicles/{id} - Elimina un vehículo de la flota
     * 
     * Criterios de aceptación:
     * 1. El vehículo debe desaparecer de la lista de disponibles
     * 2. Mostrar advertencia si tiene viajes asignados
     * 3. Confirmar eliminación para que no esté disponible
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteVehicle(@PathVariable Long id) {
        logger.info("Solicitud para eliminar vehículo ID: {}", id);

        try {
            vehicleService.deleteVehicle(id);
            
            logger.info("Vehículo eliminado exitosamente: ID={}", id);
            return ResponseEntity.ok(new SuccessResponse(
                "Vehículo eliminado exitosamente de la flota"
            ));
            
        } catch (Exception e) {
            logger.error("Error al eliminar vehículo ID {}: {}", id, e.getMessage());
            // Las excepciones específicas son manejadas por GlobalExceptionHandler
            throw e;
        }
    }

    /**
     * Lista vehículos por estado
     * GET /api/vehicles/status/{status}
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<VehicleResponse>> getVehiclesByStatus(
            @PathVariable VehicleStatus status) {
        logger.debug("Solicitud para listar vehículos por estado: {}", status);
        
        List<VehicleResponse> vehicles = vehicleService.getVehiclesByStatus(status);
        
        logger.debug("Retornando {} vehículos con estado {}", vehicles.size(), status);
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Lista vehículos disponibles para asignación
     * GET /api/vehicles/available
     */
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<VehicleResponse>> getAvailableVehicles() {
        logger.debug("Solicitud para listar vehículos disponibles");
        
        List<VehicleResponse> vehicles = vehicleService.getAvailableVehicles();
        
        logger.debug("Retornando {} vehículos disponibles", vehicles.size());
        return ResponseEntity.ok(vehicles);
    }

    /**
     * Cambia el estado de un vehículo
     * PATCH /api/vehicles/{id}/status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleResponse> changeVehicleStatus(
            @PathVariable Long id,
            @RequestBody StatusChangeRequest request) {
        logger.info("Solicitud para cambiar estado de vehículo ID: {} a {}", id, request.getStatus());
        
        VehicleResponse vehicle = vehicleService.changeVehicleStatus(id, request.getStatus());
        
        logger.info("Estado de vehículo cambiado exitosamente: ID={}, nuevo estado={}", 
                   id, request.getStatus());
        return ResponseEntity.ok(vehicle);
    }

    // Clases internas para respuestas
    public static class ErrorResponse {
        private String errorCode;
        private String message;

        public ErrorResponse(String errorCode, String message) {
            this.errorCode = errorCode;
            this.message = message;
        }

        // Getters
        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
    }

    public static class SuccessResponse {
        private String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        // Getter
        public String getMessage() { return message; }
    }

    public static class StatusChangeRequest {
        private VehicleStatus status;

        public VehicleStatus getStatus() { return status; }
        public void setStatus(VehicleStatus status) { this.status = status; }
    }
}

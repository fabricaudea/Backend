package com.fleetguard360.monitoring_service.service;

import com.fleetguard360.monitoring_service.dto.CreateVehicleRequest;
import com.fleetguard360.monitoring_service.dto.UpdateVehicleRequest;
import com.fleetguard360.monitoring_service.dto.VehicleResponse;
import com.fleetguard360.monitoring_service.model.Vehicle;
import com.fleetguard360.monitoring_service.model.VehicleStatus;
import com.fleetguard360.monitoring_service.repository.VehicleRepository;
import com.fleetguard360.monitoring_service.exception.DuplicateResourceException;
import com.fleetguard360.monitoring_service.exception.ResourceNotFoundException;
import com.fleetguard360.monitoring_service.exception.BusinessException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestión CRUD de vehículos de la flota
 */
@Service
@Transactional
@Profile("!demo")
public class VehicleService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleService.class);

    @Autowired
    private VehicleRepository vehicleRepository;

    /**
     * Crea un nuevo vehículo en la flota
     * 
     * @param request Datos del vehículo a crear
     * @return VehicleResponse con el vehículo creado
     * @throws DuplicateResourceException si ya existe un vehículo con esa placa
     */
    public VehicleResponse createVehicle(CreateVehicleRequest request) {
        logger.info("Creando nuevo vehículo con placa: {}", request.getLicensePlate());

        // Normalizar y validar placa
        String normalizedPlate = normalizeLicensePlate(request.getLicensePlate());
        
        // Verificar duplicados
        if (vehicleRepository.existsByLicensePlate(normalizedPlate)) {
            logger.warn("Intento de crear vehículo con placa duplicada: {}", normalizedPlate);
            throw new DuplicateResourceException("Ya existe un vehículo con la placa: " + normalizedPlate);
        }

        // Crear nueva entidad
        Vehicle vehicle = new Vehicle();
        mapRequestToEntity(request, vehicle);
        vehicle.setLicensePlate(normalizedPlate);
        vehicle.setCreatedBy(getCurrentUsername());

        // Guardar en base de datos
        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        logger.info("Vehículo creado exitosamente: ID={}, Placa={}", 
                   savedVehicle.getId(), savedVehicle.getLicensePlate());

        return VehicleResponse.from(savedVehicle);
    }

    /**
     * Busca un vehículo por ID
     * 
     * @param id ID del vehículo
     * @return VehicleResponse con los datos del vehículo
     * @throws ResourceNotFoundException si el vehículo no existe
     */
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleById(Long id) {
        logger.debug("Buscando vehículo por ID: {}", id);
        
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));
        
        return VehicleResponse.from(vehicle);
    }

    /**
     * Busca un vehículo por placa
     * 
     * @param licensePlate Placa del vehículo
     * @return VehicleResponse con los datos del vehículo
     * @throws ResourceNotFoundException si el vehículo no existe
     */
    @Transactional(readOnly = true)
    public VehicleResponse getVehicleByLicensePlate(String licensePlate) {
        logger.debug("Buscando vehículo por placa: {}", licensePlate);
        
        String normalizedPlate = normalizeLicensePlate(licensePlate);
        Vehicle vehicle = vehicleRepository.findByLicensePlate(normalizedPlate)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con placa: " + normalizedPlate));
        
        return VehicleResponse.from(vehicle);
    }

    /**
     * Lista todos los vehículos activos
     * 
     * @return Lista de VehicleResponse con todos los vehículos
     */
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAllVehicles() {
        logger.debug("Obteniendo lista de todos los vehículos activos");
        
        List<Vehicle> vehicles = vehicleRepository.findActiveVehicles();
        return vehicles.stream()
                .map(VehicleResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Lista vehículos por estado
     * 
     * @param status Estado del vehículo
     * @return Lista de VehicleResponse filtrada por estado
     */
    @Transactional(readOnly = true)
    public List<VehicleResponse> getVehiclesByStatus(VehicleStatus status) {
        logger.debug("Obteniendo vehículos por estado: {}", status);
        
        List<Vehicle> vehicles = vehicleRepository.findByStatus(status);
        return vehicles.stream()
                .map(VehicleResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Lista vehículos disponibles para asignación
     * 
     * @return Lista de VehicleResponse disponibles
     */
    @Transactional(readOnly = true)
    public List<VehicleResponse> getAvailableVehicles() {
        logger.debug("Obteniendo vehículos disponibles");
        
        List<Vehicle> vehicles = vehicleRepository.findAvailableVehicles();
        return vehicles.stream()
                .map(VehicleResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * Actualiza un vehículo existente
     * 
     * @param id ID del vehículo a actualizar
     * @param request Nuevos datos del vehículo
     * @return VehicleResponse con el vehículo actualizado
     * @throws ResourceNotFoundException si el vehículo no existe
     * @throws DuplicateResourceException si la nueva placa ya existe en otro vehículo
     */
    public VehicleResponse updateVehicle(Long id, UpdateVehicleRequest request) {
        logger.info("Actualizando vehículo ID: {}", id);

        // Verificar que el vehículo existe
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));

        // Normalizar nueva placa
        String normalizedPlate = normalizeLicensePlate(request.getLicensePlate());
        
        // Verificar duplicados (excluyendo el vehículo actual)
        if (vehicleRepository.existsByLicensePlateAndIdNot(normalizedPlate, id)) {
            logger.warn("Intento de actualizar con placa duplicada: {}", normalizedPlate);
            throw new DuplicateResourceException("Ya existe otro vehículo con la placa: " + normalizedPlate);
        }

        // Aplicar cambios
        mapRequestToEntity(request, vehicle);
        vehicle.setLicensePlate(normalizedPlate);
        vehicle.setUpdatedBy(getCurrentUsername());

        // Guardar cambios
        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        logger.info("Vehículo actualizado exitosamente: ID={}, Placa={}", 
                   updatedVehicle.getId(), updatedVehicle.getLicensePlate());

        return VehicleResponse.from(updatedVehicle);
    }

    /**
     * Elimina un vehículo de la flota (soft delete)
     * 
     * @param id ID del vehículo a eliminar
     * @throws ResourceNotFoundException si el vehículo no existe
     * @throws BusinessException si el vehículo está en uso y no puede ser eliminado
     */
    public void deleteVehicle(Long id) {
        logger.info("Eliminando vehículo ID: {}", id);

        // Verificar que el vehículo existe
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));

        // Verificar que el vehículo no esté en uso
        if (vehicle.isInUse()) {
            logger.warn("Intento de eliminar vehículo en uso: ID={}, Placa={}", id, vehicle.getLicensePlate());
            throw new BusinessException(
                "No se puede eliminar el vehículo " + vehicle.getLicensePlate() + 
                " porque está actualmente en uso. Debe finalizar los viajes en curso antes de eliminarlo."
            );
        }

        // Soft delete - cambiar estado a INACTIVE
        vehicle.setStatus(VehicleStatus.INACTIVE);
        vehicle.setUpdatedBy(getCurrentUsername());
        vehicleRepository.save(vehicle);

        logger.info("Vehículo eliminado (soft delete) exitosamente: ID={}, Placa={}", 
                   id, vehicle.getLicensePlate());
    }

    /**
     * Elimina permanentemente un vehículo (hard delete)
     * Solo debe usarse en casos especiales
     * 
     * @param id ID del vehículo a eliminar permanentemente
     */
    public void permanentlyDeleteVehicle(Long id) {
        logger.warn("Eliminación permanente de vehículo ID: {}", id);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));

        vehicleRepository.delete(vehicle);
        logger.info("Vehículo eliminado permanentemente: ID={}, Placa={}", id, vehicle.getLicensePlate());
    }

    /**
     * Cambia el estado de un vehículo
     * 
     * @param id ID del vehículo
     * @param newStatus Nuevo estado
     * @return VehicleResponse con el vehículo actualizado
     */
    public VehicleResponse changeVehicleStatus(Long id, VehicleStatus newStatus) {
        logger.info("Cambiando estado de vehículo ID: {} a {}", id, newStatus);

        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehículo no encontrado con ID: " + id));

        VehicleStatus oldStatus = vehicle.getStatus();
        vehicle.setStatus(newStatus);
        vehicle.setUpdatedBy(getCurrentUsername());

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        logger.info("Estado de vehículo cambiado: ID={}, {} -> {}", id, oldStatus, newStatus);

        return VehicleResponse.from(updatedVehicle);
    }

    /**
     * Normaliza el formato de la placa del vehículo
     */
    private String normalizeLicensePlate(String licensePlate) {
        if (licensePlate == null) {
            return null;
        }
        // Convertir a mayúsculas y remover espacios/guiones
        return licensePlate.toUpperCase().replaceAll("[\\s-]", "");
    }

    /**
     * Mapea los datos del request a la entidad
     */
    private void mapRequestToEntity(Object request, Vehicle vehicle) {
        if (request instanceof CreateVehicleRequest createReq) {
            vehicle.setModel(createReq.getModel());
            vehicle.setBrand(createReq.getBrand());
            vehicle.setYear(createReq.getYear());
            vehicle.setCapacity(createReq.getCapacity());
            vehicle.setStatus(createReq.getStatus());
            vehicle.setFuelType(createReq.getFuelType());
            vehicle.setMileage(createReq.getMileage());
            vehicle.setColor(createReq.getColor());
            vehicle.setNotes(createReq.getNotes());
        } else if (request instanceof UpdateVehicleRequest updateReq) {
            vehicle.setModel(updateReq.getModel());
            vehicle.setBrand(updateReq.getBrand());
            vehicle.setYear(updateReq.getYear());
            vehicle.setCapacity(updateReq.getCapacity());
            vehicle.setStatus(updateReq.getStatus());
            vehicle.setFuelType(updateReq.getFuelType());
            vehicle.setMileage(updateReq.getMileage());
            vehicle.setColor(updateReq.getColor());
            vehicle.setNotes(updateReq.getNotes());
        }
    }

    /**
     * Obtiene el nombre del usuario actual del contexto de seguridad
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && authentication.isAuthenticated()) 
                ? authentication.getName() 
                : "system";
    }
}
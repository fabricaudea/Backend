package com.fleetguard360.monitoring_service.repository;

import com.fleetguard360.monitoring_service.model.Vehicle;
import com.fleetguard360.monitoring_service.model.VehicleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para operaciones CRUD de vehículos
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    /**
     * Busca vehículo por placa (único)
     */
    Optional<Vehicle> findByLicensePlate(String licensePlate);

    /**
     * Verifica si existe un vehículo con la placa dada
     */
    boolean existsByLicensePlate(String licensePlate);

    /**
     * Verifica si existe un vehículo con la placa dada, excluyendo un ID específico
     * Útil para validaciones de actualización
     */
    boolean existsByLicensePlateAndIdNot(String licensePlate, Long id);

    /**
     * Busca vehículos por estado
     */
    List<Vehicle> findByStatus(VehicleStatus status);

    /**
     * Busca vehículos disponibles para asignación
     */
    @Query("SELECT v FROM Vehicle v WHERE v.status = 'AVAILABLE'")
    List<Vehicle> findAvailableVehicles();

    /**
     * Busca vehículos por modelo (búsqueda insensible a mayúsculas/minúsculas)
     */
    @Query("SELECT v FROM Vehicle v WHERE LOWER(v.model) LIKE LOWER(CONCAT('%', :model, '%'))")
    List<Vehicle> findByModelContainingIgnoreCase(@Param("model") String model);

    /**
     * Busca vehículos por marca
     */
    @Query("SELECT v FROM Vehicle v WHERE LOWER(v.brand) LIKE LOWER(CONCAT('%', :brand, '%'))")
    List<Vehicle> findByBrandContainingIgnoreCase(@Param("brand") String brand);

    /**
     * Busca vehículos por capacidad mínima
     */
    List<Vehicle> findByCapacityGreaterThanEqual(Integer minCapacity);

    /**
     * Busca vehículos por rango de capacidad
     */
    List<Vehicle> findByCapacityBetween(Integer minCapacity, Integer maxCapacity);

    /**
     * Busca vehículos activos (no dados de baja)
     */
    @Query("SELECT v FROM Vehicle v WHERE v.status != 'INACTIVE' ORDER BY v.licensePlate")
    List<Vehicle> findActiveVehicles();

    /**
     * Cuenta vehículos por estado
     */
    @Query("SELECT COUNT(v) FROM Vehicle v WHERE v.status = :status")
    long countByStatus(@Param("status") VehicleStatus status);

    /**
     * Obtiene estadísticas básicas de la flota
     */
    @Query("SELECT v.status, COUNT(v) FROM Vehicle v GROUP BY v.status")
    List<Object[]> getFleetStatistics();

    /**
     * Busca vehículos creados por un usuario específico
     */
    List<Vehicle> findByCreatedByOrderByCreatedAtDesc(String createdBy);
}
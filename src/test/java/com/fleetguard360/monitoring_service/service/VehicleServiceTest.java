package com.fleetguard360.monitoring_service.service;

import com.fleetguard360.monitoring_service.dto.CreateVehicleRequest;
import com.fleetguard360.monitoring_service.dto.UpdateVehicleRequest;
import com.fleetguard360.monitoring_service.dto.VehicleResponse;
import com.fleetguard360.monitoring_service.exception.BusinessException;
import com.fleetguard360.monitoring_service.exception.DuplicateResourceException;
import com.fleetguard360.monitoring_service.exception.ResourceNotFoundException;
import com.fleetguard360.monitoring_service.model.Vehicle;
import com.fleetguard360.monitoring_service.model.VehicleStatus;
import com.fleetguard360.monitoring_service.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle vehicle;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        vehicle = new Vehicle("ABC123", "Sprinter", 15, VehicleStatus.AVAILABLE);
        vehicle.setId(1L);

        // Simular usuario autenticado
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(new TestingAuthenticationToken("testUser", null));
        SecurityContextHolder.setContext(context);
    }

    @Test
    void createVehicle_Success() {
        CreateVehicleRequest request = new CreateVehicleRequest();
        request.setLicensePlate("ABC123");
        request.setModel("Sprinter");
        request.setCapacity(15);
        request.setStatus(VehicleStatus.AVAILABLE);

        when(vehicleRepository.existsByLicensePlate("ABC123")).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        VehicleResponse response = vehicleService.createVehicle(request);

        assertNotNull(response);
        assertEquals("ABC123", response.getLicensePlate());
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void createVehicle_DuplicateLicensePlate_ThrowsException() {
        CreateVehicleRequest request = new CreateVehicleRequest();
        request.setLicensePlate("ABC123");

        when(vehicleRepository.existsByLicensePlate("ABC123")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> vehicleService.createVehicle(request));
        verify(vehicleRepository, never()).save(any(Vehicle.class));
    }

    @Test
    void getVehicleById_Success() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        VehicleResponse response = vehicleService.getVehicleById(1L);

        assertNotNull(response);
        assertEquals("ABC123", response.getLicensePlate());
    }

    @Test
    void getVehicleById_NotFound_ThrowsException() {
        when(vehicleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> vehicleService.getVehicleById(99L));
    }

    @Test
    void updateVehicle_Success() {
        UpdateVehicleRequest request = new UpdateVehicleRequest();
        request.setLicensePlate("XYZ789");
        request.setModel("NPR");
        request.setStatus(VehicleStatus.AVAILABLE);

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.existsByLicensePlateAndIdNot("XYZ789", 1L)).thenReturn(false);
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        VehicleResponse response = vehicleService.updateVehicle(1L, request);

        assertNotNull(response);
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void updateVehicle_DuplicateLicensePlate_ThrowsException() {
        UpdateVehicleRequest request = new UpdateVehicleRequest();
        request.setLicensePlate("XYZ789");

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.existsByLicensePlateAndIdNot("XYZ789", 1L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> vehicleService.updateVehicle(1L, request));
    }

    @Test
    void deleteVehicle_Success() {
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        vehicleService.deleteVehicle(1L);

        verify(vehicleRepository).save(vehicle);
        assertEquals(VehicleStatus.INACTIVE, vehicle.getStatus());
    }

    @Test
    void deleteVehicle_InUse_ThrowsBusinessException() {
        vehicle.setStatus(VehicleStatus.IN_USE);
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        assertThrows(BusinessException.class, () -> vehicleService.deleteVehicle(1L));
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void permanentlyDeleteVehicle_Success() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

        vehicleService.permanentlyDeleteVehicle(1L);

        verify(vehicleRepository).delete(vehicle);
    }

    @Test
    void changeVehicleStatus_Success() {
        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
        when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);

        VehicleResponse response = vehicleService.changeVehicleStatus(1L, VehicleStatus.MAINTENANCE);

        assertNotNull(response);
        verify(vehicleRepository).save(vehicle);
        assertEquals(VehicleStatus.MAINTENANCE, vehicle.getStatus());
    }

    @Test
    void getAvailableVehicles_ReturnsList() {
        when(vehicleRepository.findAvailableVehicles()).thenReturn(List.of(vehicle));

        List<VehicleResponse> vehicles = vehicleService.getAvailableVehicles();

        assertEquals(1, vehicles.size());
        verify(vehicleRepository).findAvailableVehicles();
    }

    @Test
    void getVehiclesByStatus_ReturnsList() {
        when(vehicleRepository.findByStatus(VehicleStatus.AVAILABLE)).thenReturn(List.of(vehicle));

        List<VehicleResponse> vehicles = vehicleService.getVehiclesByStatus(VehicleStatus.AVAILABLE);

        assertEquals(1, vehicles.size());
        assertEquals("ABC123", vehicles.get(0).getLicensePlate());
    }
}

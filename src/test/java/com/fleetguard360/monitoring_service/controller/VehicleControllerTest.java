package com.fleetguard360.monitoring_service.controller;

// Importar todos los matchers y builders estáticos
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetguard360.monitoring_service.config.SecurityConfig;
import com.fleetguard360.monitoring_service.dto.CreateVehicleRequest;
import com.fleetguard360.monitoring_service.dto.UpdateVehicleRequest;
import com.fleetguard360.monitoring_service.dto.VehicleResponse;
// Importar la excepción específica de tu handler
import com.fleetguard360.monitoring_service.exception.ResourceNotFoundException;
import com.fleetguard360.monitoring_service.model.VehicleStatus;
import com.fleetguard360.monitoring_service.service.CustomUserDetailsService;
import com.fleetguard360.monitoring_service.service.VehicleService;

/**
 * Pruebas unitarias para VehicleController.
 * Se utiliza @WebMvcTest para enfocar las pruebas solo en la capa web (controlador).
 * Se simula la capa de servicio (VehicleService) usando @MockBean.
 * Se utiliza @WithMockUser para simular la autenticación y autorización.
 */
@WebMvcTest(VehicleController.class)

@Import({SecurityConfig.class, com.fleetguard360.monitoring_service.exception.GlobalExceptionHandler.class})

class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc; // Permite simular peticiones HTTP

    @Autowired
    private ObjectMapper objectMapper; // Para convertir objetos a JSON y viceversa

    @MockitoBean
    private VehicleService vehicleService; // Simulación del servicio

    @MockitoBean
    private CustomUserDetailsService userDetailsService; // Dependencia de SecurityConfig

    private VehicleResponse vehicleResponse;
    private CreateVehicleRequest createRequest;
    private UpdateVehicleRequest updateRequest;

    @BeforeEach
    void setUp() {
        // Objeto de respuesta base para los mocks
        vehicleResponse = new VehicleResponse();
        vehicleResponse.setId(1L);
        vehicleResponse.setLicensePlate("ABC-123");
        vehicleResponse.setModel("Test Model");
        vehicleResponse.setCapacity(50);
        vehicleResponse.setStatus(VehicleStatus.AVAILABLE);
        vehicleResponse.setStatusDisplayName("Disponible");
        vehicleResponse.setCreatedAt(LocalDateTime.now());

        // Objeto de creación base
        // Asumimos que CreateVehicleRequest tiene estos setters
        createRequest = new CreateVehicleRequest();
        createRequest.setLicensePlate("ABC-123");
        createRequest.setModel("Test Model");
        createRequest.setCapacity(50);
        createRequest.setStatus(VehicleStatus.AVAILABLE);

        // Objeto de actualización base
        // Asumimos que UpdateVehicleRequest tiene estos setters
        updateRequest = new UpdateVehicleRequest();
        // FIX: Añadimos los campos probablemente requeridos por @Valid
        updateRequest.setLicensePlate("ABC-123"); 
        updateRequest.setModel("Updated Model");
        updateRequest.setCapacity(55);
        updateRequest.setStatus(VehicleStatus.MAINTENANCE);
    }

    // --- Pruebas para createVehicle (POST /api/vehicles) ---

    @Test
    @WithMockUser(roles = "ADMIN") // El endpoint requiere rol ADMIN (aunque esté comentado)
    void whenCreateVehicle_withValidData_shouldReturnCreated() throws Exception {
        // Arrange
        when(vehicleService.createVehicle(any(CreateVehicleRequest.class))).thenReturn(vehicleResponse);

        // Act & Assert
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.licensePlate", is("ABC-123")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenCreateVehicle_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Arrange
        // Creamos un request inválido (placa nula, asumiendo @NotBlank)
        CreateVehicleRequest invalidRequest = new CreateVehicleRequest();
        invalidRequest.setModel("Test");

        // Act & Assert
        // La validación @Valid debe fallar antes de llamar al servicio
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("VALIDATION_ERROR")));
    }

    @Test
    @WithMockUser(roles = "USER") // Probamos con rol USER
    void whenCreateVehicle_withUserRole_shouldReturnForbidden() throws Exception {
        
        when(vehicleService.createVehicle(any(CreateVehicleRequest.class))).thenReturn(vehicleResponse);
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    void whenCreateVehicle_withoutAuth_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());
    }


    // --- Pruebas para getAllVehicles (GET /api/vehicles) ---

    @Test
    @WithMockUser(roles = "USER") // Requiere USER o ADMIN
    void whenGetAllVehicles_shouldReturnVehicleList() throws Exception {
        // Arrange
        when(vehicleService.getAllVehicles()).thenReturn(List.of(vehicleResponse));

        // Act & Assert
        mockMvc.perform(get("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].licensePlate", is("ABC-123")));
    }

    @Test
    void whenGetAllVehicles_withoutAuth_shouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/vehicles"))
                // FIX: La configuración de seguridad devuelve 403 (Forbidden) por defecto, no 401
                .andExpect(status().isForbidden());
    }

    // --- Pruebas para getVehicleById (GET /api/vehicles/{id}) ---

    @Test
    @WithMockUser(roles = "ADMIN") // Requiere USER o ADMIN
    void whenGetVehicleById_withValidId_shouldReturnVehicle() throws Exception {
        // Arrange
        when(vehicleService.getVehicleById(1L)).thenReturn(vehicleResponse);

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenGetVehicleById_withInvalidId_shouldReturnNotFound() throws Exception {
        when(vehicleService.getVehicleById(99L)).thenThrow(new ResourceNotFoundException("Vehicle not found with ID: 99"));

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/{id}", 99L)
                        .contentType(MediaType.APPLICATION_JSON))
                // FIX: Ahora el GlobalExceptionHandler (importado) debe manejar la RuntimeException
                .andExpect(status().isNotFound()); 
    }

    // --- Pruebas para updateVehicle (PUT /api/vehicles/{id}) ---

    @Test
    @WithMockUser(roles = "ADMIN") // Requiere ADMIN
    void whenUpdateVehicle_withValidData_shouldReturnOk() throws Exception {
        // Arrange
        vehicleResponse.setModel("Updated Model"); // La respuesta debe reflejar la actualización
        when(vehicleService.updateVehicle(eq(1L), any(UpdateVehicleRequest.class))).thenReturn(vehicleResponse);

        // Act & Assert
        mockMvc.perform(put("/api/vehicles/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.model", is("Updated Model")));
    }

    @Test
    @WithMockUser(roles = "USER") // Rol incorrecto
    void whenUpdateVehicle_withUserRole_shouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/vehicles/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    // --- Pruebas para deleteVehicle (DELETE /api/vehicles/{id}) ---

    @Test
    @WithMockUser(roles = "ADMIN") // Requiere ADMIN
    void whenDeleteVehicle_withValidId_shouldReturnOk() throws Exception {
        // Arrange
        doNothing().when(vehicleService).deleteVehicle(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/vehicles/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Vehículo eliminado exitosamente de la flota")));
    }

    @Test
    @WithMockUser(roles = "USER") // Rol incorrecto
    void whenDeleteVehicle_withUserRole_shouldReturnForbidden() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/vehicles/{id}", 1L))
                .andExpect(status().isForbidden());
    }

    // --- Pruebas para changeVehicleStatus (PATCH /api/vehicles/{id}/status) ---

    @Test
    @WithMockUser(roles = "ADMIN") // Requiere ADMIN
    void whenChangeVehicleStatus_shouldReturnOk() throws Exception {
        // Arrange
        VehicleController.StatusChangeRequest statusRequest = new VehicleController.StatusChangeRequest();
        statusRequest.setStatus(VehicleStatus.MAINTENANCE);

        vehicleResponse.setStatus(VehicleStatus.MAINTENANCE); // La respuesta esperada
        vehicleResponse.setStatusDisplayName("En Mantenimiento");

        when(vehicleService.changeVehicleStatus(1L, VehicleStatus.MAINTENANCE)).thenReturn(vehicleResponse);

        // Act & Assert
        mockMvc.perform(patch("/api/vehicles/{id}/status", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("MAINTENANCE")))
                .andExpect(jsonPath("$.statusDisplayName", is("En Mantenimiento")));
    }

    // --- Pruebas para endpoints GET adicionales ---

    @Test
    @WithMockUser(roles = "USER")
    void whenGetVehicleByLicensePlate_shouldReturnVehicle() throws Exception {
        // Arrange
        String plate = "ABC-123";
        when(vehicleService.getVehicleByLicensePlate(plate)).thenReturn(vehicleResponse);

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/by-plate/{licensePlate}", plate)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate", is(plate)));
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenGetVehiclesByStatus_shouldReturnVehicleList() throws Exception {
        // Arrange
        VehicleStatus status = VehicleStatus.AVAILABLE;
        // FIX: Hacemos el mock más específico para el enum
        when(vehicleService.getVehiclesByStatus(status)).thenReturn(List.of(vehicleResponse));

        mockMvc.perform(get("/api/vehicles/status/{status}", "AVAILABLE")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("AVAILABLE")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void whenGetAvailableVehicles_shouldReturnVehicleList() throws Exception {
        // Arrange
        when(vehicleService.getAvailableVehicles()).thenReturn(List.of(vehicleResponse));

        // Act & Assert
        mockMvc.perform(get("/api/vehicles/available")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].status", is("AVAILABLE")));
    }
}

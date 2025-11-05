package com.fleetguard360.monitoring_service.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
// Importaciones estáticas para Mockito y MockMvc
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq; // <--- SOLUCIÓN 1: Importar 'eq'
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleetguard360.monitoring_service.config.SecurityConfig;
import com.fleetguard360.monitoring_service.dto.LoginRequest;
import com.fleetguard360.monitoring_service.model.Role;
import com.fleetguard360.monitoring_service.model.User;
import com.fleetguard360.monitoring_service.service.AuthenticationService;
import com.fleetguard360.monitoring_service.service.CustomUserDetailsService;


@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    private LoginRequest loginRequest;
    private User mockUser;
    private Role mockRole;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest("testuser", "password123");

        mockRole = Mockito.mock(Role.class);
        when(mockRole.getName()).thenReturn("ROLE_USER");

        mockUser = new User();
        mockUser.setUsername("testuser_API");
        mockUser.setRoles(Set.of(mockRole));
        mockUser.setEnabled(true);
    }

    // --- Pruebas para el endpoint /login ---

    @Test
    void testLogin_Success() throws Exception {
        // Arrange
        String preparedUsername = "testuser_API";
        Authentication mockAuth = Mockito.mock(Authentication.class);

        when(authenticationService.prepareUsername(loginRequest.getUsername(), "API")).thenReturn(preparedUsername);
        when(authenticationService.isAccountLocked(preparedUsername)).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mockAuth);
        when(userDetailsService.loadUserEntityByUsername(preparedUsername)).thenReturn(mockUser);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.username").value(preparedUsername))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));

        verify(authenticationService).resetFailedAttempts(preparedUsername);
        verify(authenticationService, never()).recordFailedAttempt(anyString(), anyString());
    }

    @Test
    void testLogin_BadCredentials() throws Exception {
        // Arrange
        String preparedUsername = "testuser_API";

        when(authenticationService.prepareUsername(loginRequest.getUsername(), "API")).thenReturn(preparedUsername);
        when(authenticationService.isAccountLocked(preparedUsername)).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos"));

        // Verify
        // SOLUCIÓN 1: Usar eq() para el primer argumento 'preparedUsername'
        verify(authenticationService).recordFailedAttempt(eq(preparedUsername), anyString());
        verify(authenticationService, never()).resetFailedAttempts(anyString());
    }

    @Test
    void testLogin_AccountLocked_PreCheck() throws Exception {
        // Arrange
        String preparedUsername = "testuser_API";

        when(authenticationService.prepareUsername(loginRequest.getUsername(), "API")).thenReturn(preparedUsername);
        when(authenticationService.isAccountLocked(preparedUsername)).thenReturn(true);

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.status").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("Cuenta bloqueada por múltiples intentos fallidos. Intente más tarde."));

        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void testLogin_AccountLocked_DuringAuth() throws Exception {
        // Arrange
        String preparedUsername = "testuser_API";

        when(authenticationService.prepareUsername(loginRequest.getUsername(), "API")).thenReturn(preparedUsername);
        when(authenticationService.isAccountLocked(preparedUsername)).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new LockedException("Cuenta bloqueada"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.status").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("Cuenta bloqueada temporalmente"));
    }

    @Test
    void testLogin_AccountDisabled() throws Exception {
        // Arrange
        String preparedUsername = "testuser_API";

        when(authenticationService.prepareUsername(loginRequest.getUsername(), "API")).thenReturn(preparedUsername);
        when(authenticationService.isAccountLocked(preparedUsername)).thenReturn(false);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new DisabledException("Cuenta deshabilitada"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value("FAILURE"))
                .andExpect(jsonPath("$.message").value("Cuenta deshabilitada"));
    }

    @Test
    void testLogin_ValidationFailure_BlankUsername() throws Exception {
        // Arrange
        LoginRequest badRequest = new LoginRequest("", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILURE"))
                .andExpect(jsonPath("$.message").value(containsString("El nombre de usuario es obligatorio")));
    }

    @Test
    void testLogin_ValidationFailure_ShortPassword() throws Exception {
        // Arrange
        LoginRequest badRequest = new LoginRequest("testuser", "123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("FAILURE"))
                .andExpect(jsonPath("$.message").value(containsString("La contraseña debe tener al menos 6 caracteres")));
    }

    // --- Pruebas para el endpoint /logout ---

    @Test
    @WithMockUser(username = "logged_in_user")
    void testLogout_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Logout exitoso"))
                .andExpect(jsonPath("$.username").value("logged_in_user"));
    }

    // --- Pruebas para el endpoint /status ---

    @Test
    @WithMockUser(username = "active_user", authorities = {"ROLE_ADMIN", "ROLE_VIEWER"})
    void testGetAuthStatus_Authenticated() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/auth/status")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.username").value("active_user"))
                .andExpect(jsonPath("$.roles").isArray())
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ROLE_ADMIN", "ROLE_VIEWER")));
    }

    @Test
    void testGetAuthStatus_NotAuthenticated() throws Exception {
        // Act & Assert
        // SOLUCIÓN 2: El filtro de seguridad devuelve 403 Forbidden para usuarios
        // anónimos que intentan acceder a un endpoint .authenticated()
        mockMvc.perform(get("/api/auth/status")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // <-- CAMBIADO DE isUnauthorized() (401) a isForbidden() (403)
    }
}

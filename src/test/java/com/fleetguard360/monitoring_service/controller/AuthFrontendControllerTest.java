package com.fleetguard360.monitoring_service.controller;

import static org.hamcrest.Matchers.containsString;
// Importaciones estáticas para Mockito y MockMvc
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
// Importaciones estáticas de 'anonymous' y 'SecurityMockMvcRequestPostProcessors'
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
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


/**
 * Pruebas unitarias para AuthFrontendController.
 * Importa SecurityConfig para usar las mismas reglas de seguridad
 * (ej. CSRF deshabilitado) que la aplicación.
 */
@WebMvcTest(controllers = AuthFrontendController.class)
@Import(SecurityConfig.class)
public class AuthFrontendControllerTest {

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

        // Simula la clase Role (ya que no se proporcionó)
        // El controlador la usa para UserResponseFrontend.from(user)
        mockRole = Mockito.mock(Role.class);
        when(mockRole.getName()).thenReturn("ROLE_USER");

        // Configura un usuario simulado
        mockUser = new User();
        mockUser.setId(1L); // El DTO de respuesta 'UserResponseFrontend' espera un ID
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
        
        // Esta vez, el userDetailsService SÍ se usa en la ruta exitosa
        when(userDetailsService.loadUserEntityByUsername(preparedUsername)).thenReturn(mockUser);

        // Act & Assert
        mockMvc.perform(post("/api/frontend/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                // Asumiendo que UserResponseFrontend.from(user) mapea estos campos
                .andExpect(jsonPath("$.id").value(1L)) 
                .andExpect(jsonPath("$.username").value("testuser_API"));

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
        mockMvc.perform(post("/api/frontend/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()) // 401
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"))
                .andExpect(jsonPath("$.message").value("Usuario o contraseña incorrectos"));

        // Verify
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
        mockMvc.perform(post("/api/frontend/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()) // 401
                .andExpect(jsonPath("$.error").value("ACCOUNT_LOCKED"))
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
        mockMvc.perform(post("/api/frontend/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()) // 401
                .andExpect(jsonPath("$.error").value("ACCOUNT_LOCKED"))
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
        mockMvc.perform(post("/api/frontend/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized()) // 401
                .andExpect(jsonPath("$.error").value("ACCOUNT_DISABLED"))
                .andExpect(jsonPath("$.message").value("Cuenta deshabilitada"));
    }

    @Test
    void testLogin_ValidationFailure_BlankUsername() throws Exception {
        // Arrange
        LoginRequest badRequest = new LoginRequest("", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/frontend/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest()) // 400
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("El nombre de usuario es obligatorio")));
    }

    @Test
    void testLogin_ValidationFailure_ShortPassword() throws Exception {
        // Arrange
        LoginRequest badRequest = new LoginRequest("testuser", "123");

        // Act & Assert
        mockMvc.perform(post("/api/frontend/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(badRequest)))
                .andExpect(status().isBadRequest()) // 400
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value(containsString("La contraseña debe tener al menos 6 caracteres")));
    }

    // --- Pruebas para el endpoint /logout ---

    @Test
    @WithMockUser(username = "logged_in_user")
    void testLogout_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/frontend/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.message").value("Logout exitoso"));
    }

    // --- Pruebas para el endpoint /me ---

    @Test
    @WithMockUser(username = "active_user", authorities = {"ROLE_ADMIN"})
    void testGetMe_Authenticated() throws Exception {
        // Arrange
        // Preparamos un usuario que coincida con el usuario simulado por @WithMockUser
        Role authRole = Mockito.mock(Role.class);
        when(authRole.getName()).thenReturn("ROLE_ADMIN");
        
        User authUser = new User();
        authUser.setId(42L);
        authUser.setUsername("active_user");
        authUser.setRoles(Set.of(authRole));

        // El controlador llama a userDetailsService para construir la respuesta
        when(userDetailsService.loadUserEntityByUsername("active_user")).thenReturn(authUser);

        // Act & Assert
        mockMvc.perform(get("/api/frontend/auth/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42L))
                .andExpect(jsonPath("$.username").value("active_user"));
    }

    @Test
    void testGetMe_NotAuthenticated() throws Exception {
        // Act & Assert
        // Probamos que la lógica *dentro* del controlador que maneja "anonymousUser" funciona.
        // Usamos .with(anonymous()) para que el filtro de seguridad nos deje pasar
        // pero SecurityContextHolder.getContext().getAuthentication().getName() sea "anonymousUser".
        
        // CORRECCIÓN: La suposición anterior era incorrecta.
        // El filtro de seguridad SÍ se ejecuta ANTES que el controlador.
        // Como /api/frontend/auth/me es .authenticated(), el filtro
        // bloqueará al usuario anónimo con un 403 Forbidden.
        mockMvc.perform(get("/api/frontend/auth/me")
                .with(anonymous()) // Simula un usuario anónimo reconocido por Spring
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // <-- CAMBIADO DE isUnauthorized() (401) a isForbidden() (403)
                
                // Estas líneas se eliminan porque el filtro 403 devuelve un cuerpo vacío
                // .andExpect(jsonPath("$.error").value("NOT_AUTHENTICATED"))
                // .andExpect(jsonPath("$.message").value("Usuario no autenticado"));
    }

    @Test
    void testGetMe_NotAuthenticated_FilterBlock() throws Exception {
        // Act & Assert
        // Esta prueba (sin .with(anonymous())) verifica que el filtro de seguridad
        // bloquea una petición verdaderamente anónima (sin contexto de seguridad).
        // Como vimos en la prueba anterior, el filtro devuelve 403 Forbidden.
        mockMvc.perform(get("/api/frontend/auth/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // 403
    }
}

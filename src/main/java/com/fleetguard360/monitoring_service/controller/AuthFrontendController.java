package com.fleetguard360.monitoring_service.controller;

import com.fleetguard360.monitoring_service.dto.LoginRequest;
import com.fleetguard360.monitoring_service.dto.UserResponseFrontend;
import com.fleetguard360.monitoring_service.model.User;
import com.fleetguard360.monitoring_service.service.AuthenticationService;
import com.fleetguard360.monitoring_service.service.CustomUserDetailsService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para autenticación específicamente diseñado para el frontend React
 * Proporciona respuestas en el formato esperado por el frontend
 */
@RestController
@RequestMapping("/api/frontend/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthFrontendController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthFrontendController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    /**
     * Endpoint para autenticación de usuarios compatible con el frontend
     * POST /api/frontend/auth/login
     * 
     * Espera: { "username": "admin", "password": "admin123" }
     * Retorna: { "id": "1", "username": "admin", "role": "administrador", "name": "Administrador FleetGuard" }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest,
            BindingResult bindingResult,
            HttpServletRequest request) {
        
        String username = loginRequest.getUsername();
				if (username != null){
					username = username.replaceAll("[\n\r]", "_");
				} 
        logger.info("Frontend - Intento de login para usuario: {}", username);
        
        // Validar errores de entrada
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            
            logger.warn("Frontend - Errores de validación en login: {}", errorMessage);
            return ResponseEntity.badRequest().body(Map.of(
                "error", "VALIDATION_ERROR",
                "message", "Datos de entrada inválidos: " + errorMessage
            ));
        }
        
        String password = loginRequest.getPassword();
        String clientIp = getClientIpAddress(request);
        
        try {
            // Verificar si la cuenta está bloqueada
            if (authenticationService.isAccountLocked(username)) {
                logger.warn("Frontend - Intento de login en cuenta bloqueada: {} desde IP: {}", username, clientIp);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "ACCOUNT_LOCKED",
                    "message", "Cuenta bloqueada por múltiples intentos fallidos. Intente más tarde."
                ));
            }
            
            // Realizar autenticación
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            
            // Si la autenticación es exitosa, establecer el contexto de seguridad
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Crear sesión HTTP
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                                SecurityContextHolder.getContext());
            
            // Resetear intentos fallidos
            authenticationService.resetFailedAttempts(username);
            
            // Obtener información del usuario autenticado
            User user = userDetailsService.loadUserEntityByUsername(username);
            UserResponseFrontend userResponse = UserResponseFrontend.from(user);
            
            logger.info("Frontend - Login exitoso para usuario: {} desde IP: {}", username, clientIp);
            
            return ResponseEntity.ok(userResponse);
            
        } catch (LockedException e) {
            logger.warn("Frontend - Cuenta bloqueada durante autenticación: {} desde IP: {}", username, clientIp);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "ACCOUNT_LOCKED",
                "message", "Cuenta bloqueada temporalmente"
            ));
                    
        } catch (DisabledException e) {
            logger.warn("Frontend - Cuenta deshabilitada: {} desde IP: {}", username, clientIp);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "ACCOUNT_DISABLED",
                "message", "Cuenta deshabilitada"
            ));
                    
        } catch (BadCredentialsException e) {
            logger.warn("Frontend - Credenciales inválidas para usuario: {} desde IP: {}", username, clientIp);
            
            // Incrementar intentos fallidos
            authenticationService.recordFailedAttempt(username, clientIp);
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "INVALID_CREDENTIALS",
                "message", "Usuario o contraseña incorrectos"
            ));
                    
        } catch (AuthenticationException e) {
            logger.error("Frontend - Error de autenticación para usuario: {} desde IP: {}, Error: {}", 
                        username, clientIp, e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "error", "AUTHENTICATION_ERROR",
                "message", "Error de autenticación"
            ));
        }
    }
    
    /**
     * Endpoint para cerrar sesión compatible con el frontend
     * POST /api/frontend/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication() != null ? 
                SecurityContextHolder.getContext().getAuthentication().getName() : "usuario desconocido";
        
        logger.info("Frontend - Logout para usuario: {}", username);
        
        try {
            // Invalidar la sesión HTTP
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            // Limpiar el contexto de seguridad
            SecurityContextHolder.clearContext();
            
            logger.info("Frontend - Logout exitoso para usuario: {}", username);
            
            return ResponseEntity.ok(Map.of(
                "message", "Logout exitoso",
                "status", "SUCCESS"
            ));
            
        } catch (Exception e) {
            logger.error("Frontend - Error durante logout para usuario: {}, Error: {}", username, e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "LOGOUT_ERROR",
                "message", "Error durante el logout"
            ));
        }
    }
    
    /**
     * Endpoint para verificar el estado de autenticación
     * GET /api/frontend/auth/me
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "error", "NOT_AUTHENTICATED",
                    "message", "Usuario no autenticado"
                ));
            }
            
            String username = authentication.getName();
            User user = userDetailsService.loadUserEntityByUsername(username);
            UserResponseFrontend userResponse = UserResponseFrontend.from(user);
            
            return ResponseEntity.ok(userResponse);
            
        } catch (Exception e) {
            logger.error("Frontend - Error al obtener usuario actual: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "error", "INTERNAL_ERROR",
                "message", "Error interno del servidor"
            ));
        }
    }
    
    /**
     * Obtiene la dirección IP del cliente
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            return xForwardedForHeader.split(",")[0].trim();
        }
    }
}

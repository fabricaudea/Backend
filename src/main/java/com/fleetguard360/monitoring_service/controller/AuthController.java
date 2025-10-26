package com.fleetguard360.monitoring_service.controller;

import com.fleetguard360.monitoring_service.dto.LoginRequest;
import com.fleetguard360.monitoring_service.dto.LoginResponse;
import com.fleetguard360.monitoring_service.dto.LogoutResponse;
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

import java.util.stream.Collectors;

/**
 * Controlador REST para autenticación de usuarios
 * Maneja los endpoints de login y logout
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    /**
     * Endpoint para autenticación de usuarios
     * POST /api/auth/login
     * 
     * @param loginRequest Datos de login (username, password)
     * @param bindingResult Resultado de la validación
     * @param request HTTP request para obtener IP
     * @return ResponseEntity con LoginResponse
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest,
            BindingResult bindingResult,
            HttpServletRequest request) {
        
        String username = loginRequest.getUsername();
				if (username != null) {
					username = username.replaceAll("[\n\r]", "_");
				}
        logger.info("Intento de login para usuario: {}", username);
        
        // Validar errores de entrada
        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getAllErrors().stream()
                    .map(error -> error.getDefaultMessage())
                    .collect(Collectors.joining(", "));
            
            logger.warn("Errores de validación en login: {}", errorMessage);
            return ResponseEntity.badRequest()
                    .body(LoginResponse.failure("Datos de entrada inválidos: " + errorMessage));
        }
        
        String password = loginRequest.getPassword();
        String clientIp = getClientIpAddress(request);
				if (clientIp != null) {
					clientIp = clientIp.replaceAll("[\n\r]", "_");
				}
        
        try {
            // Verificar si la cuenta está bloqueada
            if (authenticationService.isAccountLocked(username)) {
                logger.warn("Intento de login en cuenta bloqueada: {} desde IP: {}", username, clientIp);
                return ResponseEntity.status(HttpStatus.LOCKED)
                        .body(LoginResponse.failure("Cuenta bloqueada por múltiples intentos fallidos. Intente más tarde."));
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
            var roles = user.getRoles().stream()
                    .map(role -> role.getName())
                    .collect(Collectors.toSet());
            
            logger.info("Login exitoso para usuario: {} desde IP: {}", username, clientIp);
            
            return ResponseEntity.ok(LoginResponse.success(username, roles));
            
        } catch (LockedException e) {
            logger.warn("Cuenta bloqueada durante autenticación: {} desde IP: {}", username, clientIp);
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(LoginResponse.failure("Cuenta bloqueada temporalmente"));
                    
        } catch (DisabledException e) {
            logger.warn("Cuenta deshabilitada: {} desde IP: {}", username, clientIp);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(LoginResponse.failure("Cuenta deshabilitada"));
                    
        } catch (BadCredentialsException e) {
            logger.warn("Credenciales inválidas para usuario: {} desde IP: {}", username, clientIp);
            
            // Incrementar intentos fallidos
            authenticationService.recordFailedAttempt(username, clientIp);
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.failure("Usuario o contraseña incorrectos"));
                    
        } catch (AuthenticationException e) {
            logger.error("Error de autenticación para usuario: {} desde IP: {}, Error: {}", 
                        username, clientIp, e.getMessage());
            
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.failure("Error de autenticación"));
        }
    }
    
    /**
     * Endpoint para cerrar sesión
     * POST /api/auth/logout
     * 
     * @param request HTTP request
     * @return ResponseEntity con LogoutResponse
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        
        String username = getCurrentUsername();
        String clientIp = getClientIpAddress(request);
        
        logger.info("Logout solicitado por usuario: {} desde IP: {}", username, clientIp);
        
        try {
            // Invalidar la sesión HTTP
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
                logger.debug("Sesión HTTP invalidada para usuario: {}", username);
            }
            
            // Limpiar el contexto de seguridad
            SecurityContextHolder.clearContext();
            
            logger.info("Logout exitoso para usuario: {} desde IP: {}", username, clientIp);
            
            return ResponseEntity.ok(LogoutResponse.success(username));
            
        } catch (Exception e) {
            logger.error("Error durante logout para usuario: {} desde IP: {}, Error: {}", 
                        username, clientIp, e.getMessage());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LogoutResponse.failure("Error durante el logout"));
        }
    }
    
    /**
     * Endpoint para verificar el estado de autenticación
     * GET /api/auth/status
     * 
     * @return ResponseEntity con información del usuario actual
     */
    @GetMapping("/status")
    public ResponseEntity<?> getAuthStatus() {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() 
                && !authentication.getName().equals("anonymousUser")) {
            
            String username = authentication.getName();
            var authorities = authentication.getAuthorities().stream()
                    .map(grantedAuthority -> grantedAuthority.getAuthority())
                    .collect(Collectors.toSet());
            
            return ResponseEntity.ok(LoginResponse.success(username, authorities));
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(LoginResponse.failure("Usuario no autenticado"));
    }
    
    /**
     * Obtiene el nombre de usuario actual del contexto de seguridad
     * 
     * @return username o "anonymous" si no está autenticado
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && authentication.isAuthenticated()) 
                ? authentication.getName() 
                : "anonymous";
    }
    
    /**
     * Obtiene la dirección IP del cliente desde el HTTP request
     * 
     * @param request HTTP request
     * @return dirección IP del cliente
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}

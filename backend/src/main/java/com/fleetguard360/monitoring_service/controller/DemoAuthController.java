package com.fleetguard360.monitoring_service.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Controlador de autenticación SOLO para perfil 'demo'.
 * Usa usuarios en memoria definidos en SecurityConfigDemo.
 * No toca BD ni servicios de dominio.
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@Profile("demo")
public class DemoAuthController {

    private static final Logger logger = LoggerFactory.getLogger(DemoAuthController.class);

    private final AuthenticationManager authenticationManager;

    public DemoAuthController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    // DTOs mínimos para no depender de los del dominio (puedes reutilizar los tuyos si prefieres)
    public static record LoginRequest(String username, String password) {}
    public static record LoginResponse(boolean ok, String username, Set<String> roles, String message) {
        static LoginResponse success(String u, Set<String> r) {
            return new LoginResponse(true, u, r, "OK");
        }
        static LoginResponse failure(String msg) {
            return new LoginResponse(false, null, null, msg);
        }
    }
    public static record LogoutResponse(boolean ok, String username, String message) {
        static LogoutResponse success(String u) { return new LogoutResponse(true, u, "Logout OK"); }
        static LogoutResponse failure(String msg) { return new LogoutResponse(false, null, msg); }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request
    ) {
        String username = loginRequest.username();
        String clientIp = getClientIpAddress(request);
        logger.info("[DEMO] Intento de login usuario={} ip={}", username, clientIp);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, loginRequest.password())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            HttpSession session = request.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );

            Set<String> roles = authentication.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .collect(Collectors.toSet());

            logger.info("[DEMO] Login OK usuario={} roles={}", username, roles);
            return ResponseEntity.ok(LoginResponse.success(username, roles));

        } catch (BadCredentialsException e) {
            logger.warn("[DEMO] Credenciales inválidas usuario={} ip={}", username, clientIp);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.failure("Usuario o contraseña incorrectos"));
        } catch (Exception e) {
            logger.error("[DEMO] Error autenticación usuario={} ip={} err={}", username, clientIp, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.failure("Error de autenticación"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String username = getCurrentUsername();
        String clientIp = getClientIpAddress(request);
        logger.info("[DEMO] Logout usuario={} ip={}", username, clientIp);
        try {
            HttpSession session = request.getSession(false);
            if (session != null) session.invalidate();
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(LogoutResponse.success(username));
        } catch (Exception e) {
            logger.error("[DEMO] Error logout usuario={} ip={} err={}", username, clientIp, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LogoutResponse.failure("Error durante el logout"));
        }
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean autenticado = auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName());
        if (!autenticado) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(LoginResponse.failure("Usuario no autenticado"));
        }
        Set<String> roles = auth.getAuthorities().stream().map(a -> a.getAuthority()).collect(Collectors.toSet());
        return ResponseEntity.ok(LoginResponse.success(auth.getName(), roles));
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication != null && authentication.isAuthenticated())
                ? authentication.getName()
                : "anonymous";
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) return xff.split(",")[0].trim();
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isEmpty()) return xri;
        return request.getRemoteAddr();
    }
}

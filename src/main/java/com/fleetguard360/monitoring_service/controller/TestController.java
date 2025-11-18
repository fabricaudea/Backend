package com.fleetguard360.monitoring_service.controller;

import com.fleetguard360.monitoring_service.model.User;
import com.fleetguard360.monitoring_service.service.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);

    private final CustomUserDetailsService userDetailsService;

    public TestController(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    /**
     * Endpoint completamente público.
     * No requiere token.
     */
    @GetMapping("/public")
    public ResponseEntity<?> publicEndpoint() {
        return ResponseEntity.ok(
                Map.of(
                        "endpoint", "/api/test/public",
                        "message", "Acceso público sin autenticación",
                        "status", "OK"
                )
        );
    }

    /**
     * Endpoint que muestra información básica del contexto de seguridad.
     * No exige rol específico, pero solo tendrá usuario si se envía token.
     */
    @GetMapping("/whoami")
    public ResponseEntity<?> whoAmI() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.ok(
                    Map.of(
                            "authenticated", false,
                            "message", "No hay usuario autenticado (o no se envió token)"
                    )
            );
        }

        String username = authentication.getName();
        User user = userDetailsService.loadUserEntityByUsername(username);

        return ResponseEntity.ok(
                Map.of(
                        "authenticated", true,
                        "username", username,
                        "roles", authentication.getAuthorities()
                       // "fullName", user.getFullName()
                )
        );
    }

    /**
     * Solo accesible para usuarios con rol USER o ADMIN.
     * Requiere token JWT válido.
     */
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/user")
    public ResponseEntity<?> userEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "desconocido";

        logger.info("Acceso a /api/test/user por {}", username);

        return ResponseEntity.ok(
                Map.of(
                        "endpoint", "/api/test/user",
                        "message", "Acceso permitido a usuario con rol USER o ADMIN",
                        "username", username
                )
        );
    }

    /**
     * Solo accesible para usuarios con rol ADMIN.
     * Requiere token JWT válido.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public ResponseEntity<?> adminEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "desconocido";

        logger.info("Acceso a /api/test/admin por {}", username);

        return ResponseEntity.ok(
                Map.of(
                        "endpoint", "/api/test/admin",
                        "message", "Acceso permitido solo a ADMIN",
                        "username", username
                )
        );
    }
}

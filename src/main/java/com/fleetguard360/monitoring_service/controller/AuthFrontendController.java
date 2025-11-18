package com.fleetguard360.monitoring_service.controller;

import com.fleetguard360.monitoring_service.dto.LoginRequest;              // Ajusta el paquete si tu DTO está en otro
import com.fleetguard360.monitoring_service.dto.UserResponseFrontend;     // Ajusta el paquete si es necesario
import com.fleetguard360.monitoring_service.model.User;
import com.fleetguard360.monitoring_service.service.CustomUserDetailsService;
import com.fleetguard360.monitoring_service.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/frontend/auth")
public class AuthFrontendController {

    private static final Logger logger = LoggerFactory.getLogger(AuthFrontendController.class);

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public AuthFrontendController(AuthenticationManager authenticationManager,
                                  CustomUserDetailsService userDetailsService,
                                  JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {

        String username = request.getUsername();
        String clientIp = httpRequest.getRemoteAddr();

        logger.info("Frontend - Intento de login para usuario: {} desde IP: {}", username, clientIp);

        // 1. Autenticar credenciales
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2. Guardar autenticación en el contexto de seguridad
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // (Opcional) Crear sesión HTTP si la usas para auditoría
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        // 3. Cargar entidad User desde la BD
        User user = userDetailsService.loadUserEntityByUsername(username);

        // 4. Generar token JWT
        String token = jwtUtil.generateToken(username);

        // 5. Construir respuesta para el frontend a partir del DTO existente
        UserResponseFrontend userResponse = UserResponseFrontend.from(user);

        logger.info("Frontend - Login exitoso para usuario: {} desde IP: {}", username, clientIp);

        // Devolvemos un JSON con user + token, sin modificar el DTO
        return ResponseEntity.ok(
                Map.of(
                        "message", "Login exitoso",
                        "loginTime", LocalDateTime.now().toString(),
                        "user", userResponse,
                        "token", token
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(401).body(
                    Map.of("message", "No hay usuario autenticado")
            );
        }

        String username = authentication.getName();
        User user = userDetailsService.loadUserEntityByUsername(username);
        UserResponseFrontend userResponse = UserResponseFrontend.from(user);

        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        logger.info("Frontend - Logout ejecutado correctamente");

        return ResponseEntity.ok(
                Map.of("message", "Logout exitoso")
        );
    }
}

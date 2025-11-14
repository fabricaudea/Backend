package com.fleetguard360.monitoring_service.controller;

import com.fleetguard360.monitoring_service.model.User;
import com.fleetguard360.monitoring_service.model.Role;
import com.fleetguard360.monitoring_service.repository.UserRepository;
import com.fleetguard360.monitoring_service.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*", maxAge = 3600)
@Profile("!demo")
public class TestController {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/roles")
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    /**
     * Endpoint para desbloquear y reinicializar usuarios de prueba
     */
    @PostMapping("/unlock-users")
    public ResponseEntity<?> unlockUsers() {
        try {
            // Desbloquear usuario admin
            unlockUser("admin", "admin123", "ADMIN");
            
            // Desbloquear usuario operador con contraseña más larga
            unlockUser("operador", "operador123", "USER");
            
            return ResponseEntity.ok(Map.of(
                "message", "Usuarios desbloqueados y reinicializados",
                "users", Map.of(
                    "admin", "admin123 (ADMIN role)",
                    "operador", "operador123 (USER role)"
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error al desbloquear usuarios",
                "message", e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint para verificar si las contraseñas coinciden
     */
    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "found", false,
                "message", "Usuario no encontrado"
            ));
        }
        
        User user = userOpt.get();
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        
        return ResponseEntity.ok(Map.of(
            "found", true,
            "username", username,
            "passwordMatches", matches,
            "enabled", user.isEnabled(),
            "failedAttempts", user.getFailedAttempts(),
            "lockTime", user.getLockTime(),
            "roles", user.getRoles().stream().map(role -> role.getName()).toList()
        ));
    }
    
    /**
     * Endpoint para cambiar contraseña de un usuario específico
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String newPassword = request.get("password");
            
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Usuario no encontrado",
                    "message", "El usuario " + username + " no existe"
                ));
            }
            
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setFailedAttempts(0);
            user.setLockTime(null);
            user.setEnabled(true);
            
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of(
                "message", "Contraseña actualizada exitosamente",
                "username", username,
                "newPassword", newPassword
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error al cambiar contraseña",
                "message", e.getMessage()
            ));
        }
    }
    
    private void unlockUser(String username, String password, String roleName) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            // Desbloquear cuenta
            user.setFailedAttempts(0);
            user.setLockTime(null);
            user.setEnabled(true);
            
            // Actualizar contraseña
            user.setPassword(passwordEncoder.encode(password));
            
            // Asegurar que tiene el rol correcto
            user.getRoles().clear();
            Optional<Role> role = roleRepository.findByName(roleName);
            if (role.isPresent()) {
                user.getRoles().add(role.get());
            }
            
            userRepository.save(user);
        }
    }
}
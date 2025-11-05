package com.fleetguard360.monitoring_service.controller;

import com.fleetguard360.monitoring_service.model.User;
import com.fleetguard360.monitoring_service.model.Role;
import com.fleetguard360.monitoring_service.repository.UserRepository;
import com.fleetguard360.monitoring_service.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {

		private static final String MESSAGE = "message";

		private static final String ERROR_STRING = "error";

		private static final String USERNAME_STRING = "username";

    private UserRepository userRepository;
    
    private RoleRepository roleRepository;
    
    private PasswordEncoder passwordEncoder;

		@Autowired
		public TestController ( UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
			this.userRepository = userRepository;
			this.roleRepository = roleRepository;
			this.passwordEncoder = passwordEncoder;
		}

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
                MESSAGE, "Usuarios desbloqueados y reinicializados",
                "users", Map.of(
                    "admin", "admin123 (ADMIN role)",
                    "operador", "operador123 (USER role)"
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                ERROR_STRING, "Error al desbloquear usuarios",
                MESSAGE, e.getMessage()
            ));
        }
    }
    
    /**
     * Endpoint para verificar si las contraseñas coinciden
     */
    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody Map<String, String> request) {
        String username = request.get(USERNAME_STRING);
        String password = request.get("password");
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                "found", false,
                MESSAGE, "Usuario no encontrado"
            ));
        }
        
        User user = userOpt.get();
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        
        return ResponseEntity.ok(Map.of(
            "found", true,
            USERNAME_STRING, username,
            "passwordMatches", matches,
            "enabled", user.isEnabled(),
            "failedAttempts", user.getFailedAttempts(),
            "lockTime", user.getLockTime(),
            "roles", user.getRoles().stream().map(Role::getName).toList()
        ));
    }
    
    /**
     * Endpoint para cambiar contraseña de un usuario específico
     */
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            String username = request.get(USERNAME_STRING);
            String newPassword = request.get("password");
            
            Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    ERROR_STRING, "Usuario no encontrado",
                    MESSAGE, "El usuario " + username + " no existe"
                ));
            }
            
            User user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setFailedAttempts(0);
            user.setLockTime(null);
            user.setEnabled(true);
            
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of(
                MESSAGE, "Contraseña actualizada exitosamente",
                USERNAME_STRING, username,
                "newPassword", newPassword
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                ERROR_STRING, "Error al cambiar contraseña",
                MESSAGE, e.getMessage()
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
package com.fleetguard360.monitoring_service.config;

import com.fleetguard360.monitoring_service.model.Role;
import com.fleetguard360.monitoring_service.model.User;
import com.fleetguard360.monitoring_service.repository.RoleRepository;
import com.fleetguard360.monitoring_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("Initializing database with test data...");
        
        // Create roles if they don't exist
        createRoleIfNotExists("ADMIN");
        createRoleIfNotExists("USER");
        
        // Create admin user if it doesn't exist
        createAdminUserIfNotExists();
        
        logger.info("Database initialization completed.");
    }
    
    private void createRoleIfNotExists(String roleName) {
        Optional<Role> existingRole = roleRepository.findByName(roleName);
        if (existingRole.isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            logger.info("Created role: {}", roleName);
        } else {
            logger.info("Role {} already exists", roleName);
        }
    }
    
    private void createAdminUserIfNotExists() {
        Optional<User> existingUser = userRepository.findByUsername("admin");
        if (existingUser.isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Contraseña encriptada
            admin.setEnabled(true);
            
            // Add ADMIN role
            Optional<Role> adminRole = roleRepository.findByName("ADMIN");
            if (adminRole.isPresent()) {
                admin.getRoles().add(adminRole.get());
            }
            
            userRepository.save(admin);
            logger.info("Created admin user with encrypted password");
        } else {
            logger.info("Admin user already exists");
        }
    }
}
package com.fleetguard360.monitoring_service.config;

import com.fleetguard360.monitoring_service.model.Role;
import com.fleetguard360.monitoring_service.model.User;
import com.fleetguard360.monitoring_service.model.Vehicle;
import com.fleetguard360.monitoring_service.model.VehicleStatus;
import com.fleetguard360.monitoring_service.repository.RoleRepository;
import com.fleetguard360.monitoring_service.repository.UserRepository;
import com.fleetguard360.monitoring_service.repository.VehicleRepository;
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

		private static final String SYSTEM = "system";
    
    private UserRepository userRepository;
    
    private RoleRepository roleRepository;
    
    private VehicleRepository vehicleRepository;
    
    private PasswordEncoder passwordEncoder;

		@Autowired
		public DataInitializer ( UserRepository userRepository, RoleRepository roleRepository, VehicleRepository vehicleRepository, PasswordEncoder passwordEncoder) {
			this.userRepository = userRepository;
			this.roleRepository = roleRepository;
			this.vehicleRepository = vehicleRepository;
			this.passwordEncoder = passwordEncoder;
		}
    
    @Override
    public void run(String... args) throws Exception {
			try {
				logger.info("Initializing database with test data...");

				// Create roles if they don't exist
				createRoleIfNotExists("ADMIN");
				createRoleIfNotExists("USER");

				// Create admin user if it doesn't exist
				createAdminUserIfNotExists();

				// Create operator user if it doesn't exist
				createOperatorUserIfNotExists();

				// Create sample vehicles if they don't exist
				createSampleVehiclesIfNotExists();

				logger.info("Database initialization completed.");
			} catch (Exception e) {
				logger.warn("could not initialize data: {}", e.getMessage());
			}

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

						String rawPassword = System.getenv("ADMIN_DEFAULT_PASSWORD");

						if (rawPassword == null || rawPassword.isBlank()){
							rawPassword = "admin123";
							logger.warn("No esta definida la variable de entorno ADMIN_DEFAULT_PASSWORD");
						}

            admin.setPassword(passwordEncoder.encode(rawPassword)); // Contraseña encriptada
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
    
    private void createOperatorUserIfNotExists() {
        Optional<User> existingUser = userRepository.findByUsername("operador");
        if (existingUser.isEmpty()) {
            User operator = new User();
            operator.setUsername("operador");

						String rawPassword = System.getenv("OPERATOR_DEFAULT_PASSWORD");

						if (rawPassword == null || rawPassword.isBlank()){
							rawPassword = "admin123";
							logger.warn("No esta definida la variable de entorno OPERATOR_DEFAULT_PASSWORD");
						}

            operator.setPassword(passwordEncoder.encode(rawPassword)); // Contraseña encriptada
            operator.setEnabled(true);
            
            // Add USER role
            Optional<Role> userRole = roleRepository.findByName("USER");
            if (userRole.isPresent()) {
                operator.getRoles().add(userRole.get());
            }
            
            userRepository.save(operator);
            logger.info("Created operator user with encrypted password");
        } else {
            logger.info("Operator user already exists");
        }
    }
    
    private void createSampleVehiclesIfNotExists() {
        // Verificar si ya existen vehículos
        if (vehicleRepository.count() == 0) {
            logger.info("Creating sample vehicles...");
            
            // Vehículo 1 - similar a los datos mock del frontend
            Vehicle vehicle1 = new Vehicle();
            vehicle1.setLicensePlate("ABC-123");
            vehicle1.setModel("Mercedes Sprinter 2023");
            vehicle1.setCapacity(12);
            vehicle1.setStatus(VehicleStatus.AVAILABLE);
            vehicle1.setCreatedBy(SYSTEM);
            vehicleRepository.save(vehicle1);
            
            // Vehículo 2
            Vehicle vehicle2 = new Vehicle();
            vehicle2.setLicensePlate("DEF-456");
            vehicle2.setModel("Ford Transit 2022");
            vehicle2.setCapacity(8);
            vehicle2.setStatus(VehicleStatus.MAINTENANCE);
            vehicle2.setCreatedBy(SYSTEM);
            vehicleRepository.save(vehicle2);
            
            // Vehículo 3
            Vehicle vehicle3 = new Vehicle();
            vehicle3.setLicensePlate("GHI-789");
            vehicle3.setModel("Iveco Daily 2023");
            vehicle3.setCapacity(15);
            vehicle3.setStatus(VehicleStatus.AVAILABLE);
            vehicle3.setCreatedBy(SYSTEM);
            vehicleRepository.save(vehicle3);
            
            logger.info("Created {} sample vehicles", 3);
        } else {
            logger.info("Vehicles already exist in database");
        }
    }
}

package com.fleetguard360.monitoring_service.config;

import com.fleetguard360.monitoring_service.model.Vehicle;
import com.fleetguard360.monitoring_service.model.VehicleStatus;
import com.fleetguard360.monitoring_service.repository.VehicleRepository;
import org.springframework.stereotype.Component;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class DataInitializer implements CommandLineRunner {

    private final VehicleRepository vehicleRepository;

    @Autowired
    public DataInitializer(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("🚀 Inicializando datos de vehículos...");

        vehicleRepository.save(new Vehicle("ABC-123", "Mercedes Sprinter 2023", 12, VehicleStatus.AVAILABLE));
        vehicleRepository.save(new Vehicle("DEF-456", "Ford Transit 2022", 10, VehicleStatus.MAINTENANCE));
        vehicleRepository.save(new Vehicle("GHI-789", "Iveco Daily 2023", 14, VehicleStatus.AVAILABLE));
        vehicleRepository.save(new Vehicle("JKL-012", "Volkswagen Crafter 2023", 15, VehicleStatus.IN_USE));
        vehicleRepository.save(new Vehicle("MNO-345", "Renault Master 2022", 8, VehicleStatus.AVAILABLE));

        System.out.println("✅ 5 vehículos de prueba cargados exitosamente");
    }
}

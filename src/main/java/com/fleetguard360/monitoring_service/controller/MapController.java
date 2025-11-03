package com.fleetguard360.monitoring_service.controller;

import com.fleetguard360.monitoring_service.model.Vehicle;
import com.fleetguard360.monitoring_service.repository.VehicleRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/map")
public class MapController {

    private final VehicleRepository vehicleRepository;

    public MapController(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    // Página HTML del mapa
    @GetMapping
    public String showMapPage() {
        return "map/index"; // busca en src/main/resources/templates/map/index.html (Thymeleaf)
    }

    // Endpoint para obtener ubicaciones
    @GetMapping("/vehicles")
    @ResponseBody
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }
}

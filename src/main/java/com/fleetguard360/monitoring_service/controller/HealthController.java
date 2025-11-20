package com.fleetguard360.monitoring_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

/**
 * Controlador de Salud - Verifica el estado del servicio
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "Endpoint de salud del servicio")
public class HealthController {

    @GetMapping
    @Operation(summary = "Verificar estado del servicio")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "FleetGuard360 - Monitoring Service");
        response.put("timestamp", LocalDateTime.now());
        response.put("version", "0.0.1-SNAPSHOT");

        return ResponseEntity.ok(response);
    }
}

package com.fleetguard360.monitoring_service.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Profile("demo") // <-- solo se carga en el perfil demo
public class DemoHealthController {

    // Salud de la app (sin BD)
    @GetMapping("/app")
    public ResponseEntity<Map<String, Object>> app() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "application", "FleetGuard360 Monitoring Service (DEMO)",
                "profile", "demo",
                "timestamp", OffsetDateTime.now().toString(),
                "message", "Application is running (no DB)"
        ));
    }

    // “Chequeo” de BD simulado (para que el frontend no falle)
    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> db() {
        return ResponseEntity.ok(Map.of(
                "status", "SKIPPED",
                "database", "disabled-in-demo",
                "profile", "demo",
                "message", "DB check skipped in demo profile"
        ));
    }
}

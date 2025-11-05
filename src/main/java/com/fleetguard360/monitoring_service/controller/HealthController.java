package com.fleetguard360.monitoring_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

		private static final String STATUS = "status";

		private static final String MESSAGE = "message";

    @GetMapping("/db")
    public Map<String, Object> checkDatabaseConnection() {
        Map<String, Object> response = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            response.put(STATUS, "UP");
            response.put("database", connection.getMetaData().getDatabaseProductName());
            response.put("version", connection.getMetaData().getDatabaseProductVersion());
            response.put("url", connection.getMetaData().getURL());
            response.put(MESSAGE, "Database connection successful");
        } catch (Exception e) {
            response.put(STATUS, "DOWN");
            response.put("error", e.getMessage());
            response.put(MESSAGE, "Database connection failed");
        }
        
        return response;
    }

    @GetMapping("/app")
    public Map<String, String> checkApplication() {
        Map<String, String> response = new HashMap<>();
        response.put(STATUS, "UP");
        response.put("application", "FleetGuard360 Monitoring Service");
        response.put(MESSAGE, "Application is running");
        return response;
    }
}
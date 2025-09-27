package com.fleetguard360.monitoring_service.dto;

import com.fleetguard360.monitoring_service.model.User;

/**
 * DTO para respuesta de usuario compatible con el frontend
 */
public class UserResponseFrontend {

    private String id;
    private String username;
    private String role;
    private String name;

    // Constructor vacío
    public UserResponseFrontend() {}

    // Constructor desde entidad User
    public UserResponseFrontend(User user) {
        this.id = user.getId().toString();
        this.username = user.getUsername();
        this.role = mapRoleToFrontend(user);
        this.name = generateDisplayName(user);
    }

    /**
     * Mapea los roles del backend a los esperados por el frontend
     */
    private String mapRoleToFrontend(User user) {
        if (user.getRoles().isEmpty()) {
            return "operador";
        }
        
        // Buscar si tiene rol de ADMIN
        boolean isAdmin = user.getRoles().stream()
            .anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getName()) || 
                             "ADMINISTRADOR".equalsIgnoreCase(role.getName()));
        
        return isAdmin ? "administrador" : "operador";
    }

    /**
     * Genera un nombre para mostrar basado en el usuario
     */
    private String generateDisplayName(User user) {
        String role = mapRoleToFrontend(user);
        return "administrador".equals(role) ? 
               "Administrador FleetGuard" : 
               "Operador de Flota";
    }

    // Static factory method
    public static UserResponseFrontend from(User user) {
        return new UserResponseFrontend(user);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "UserResponseFrontend{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
package com.fleetguard360.monitoring_service.dto;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO para la respuesta de login exitoso
 */
public class LoginResponse {
    
    private String message;
    private String username;
    private Set<String> roles;
    private LocalDateTime loginTime;
    private String status;
    
    // Constructors
    public LoginResponse() {}
    
    public LoginResponse(String message, String username, Set<String> roles, String status) {
        this.message = message;
        this.username = username;
        this.roles = roles;
        this.loginTime = LocalDateTime.now();
        this.status = status;
    }
    
    // Static factory methods
    public static LoginResponse success(String username, Set<String> roles) {
        return new LoginResponse("Login exitoso", username, roles, "SUCCESS");
    }
    
    public static LoginResponse failure(String message) {
        LoginResponse response = new LoginResponse();
        response.setMessage(message);
        response.setStatus("FAILURE");
        response.setLoginTime(LocalDateTime.now());
        return response;
    }
    
    // Getters and Setters
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public Set<String> getRoles() {
        return roles;
    }
    
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
    
    public LocalDateTime getLoginTime() {
        return loginTime;
    }
    
    public void setLoginTime(LocalDateTime loginTime) {
        this.loginTime = loginTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "LoginResponse{" +
                "message='" + message + '\'' +
                ", username='" + username + '\'' +
                ", roles=" + roles +
                ", loginTime=" + loginTime +
                ", status='" + status + '\'' +
                '}';
    }
}
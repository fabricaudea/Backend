package com.fleetguard360.monitoring_service.dto;

import java.time.LocalDateTime;

/**
 * DTO para la respuesta de logout
 */
public class LogoutResponse {
    
    private String message;
    private String username;
    private LocalDateTime logoutTime;
    private String status;
    
    // Constructors
    public LogoutResponse() {}
    
    public LogoutResponse(String message, String username, String status) {
        this.message = message;
        this.username = username;
        this.logoutTime = LocalDateTime.now();
        this.status = status;
    }
    
    // Static factory methods
    public static LogoutResponse success(String username) {
        return new LogoutResponse("Logout exitoso", username, "SUCCESS");
    }
    
    public static LogoutResponse failure(String message) {
        LogoutResponse response = new LogoutResponse();
        response.setMessage(message);
        response.setStatus("FAILURE");
        response.setLogoutTime(LocalDateTime.now());
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
    
    public LocalDateTime getLogoutTime() {
        return logoutTime;
    }
    
    public void setLogoutTime(LocalDateTime logoutTime) {
        this.logoutTime = logoutTime;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @Override
    public String toString() {
        return "LogoutResponse{" +
                "message='" + message + '\'' +
                ", username='" + username + '\'' +
                ", logoutTime=" + logoutTime +
                ", status='" + status + '\'' +
                '}';
    }
}
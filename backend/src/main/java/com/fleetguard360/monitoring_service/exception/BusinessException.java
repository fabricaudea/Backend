package com.fleetguard360.monitoring_service.exception;

/**
 * Excepción para violaciones de reglas de negocio
 */
public class BusinessException extends RuntimeException {
    
    public BusinessException(String message) {
        super(message);
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
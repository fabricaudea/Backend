package com.fleetguard360.monitoring_service.exception;

/**
 * Excepción lanzada cuando se intenta crear un recurso que ya existe
 */
public class DuplicateResourceException extends RuntimeException {
    
    public DuplicateResourceException(String message) {
        super(message);
    }
    
    public DuplicateResourceException(String message, Throwable cause) {
        super(message, cause);
    }
}
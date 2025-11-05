package com.fleetguard360.monitoring_service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class para generar hashes BCrypt de contraseñas
 * Útil para crear datos de prueba
 */
public class PasswordHashGenerator {

    private static final Logger logger = LoggerFactory.getLogger(PasswordHashGenerator.class);
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Contraseñas a encriptar
        String[] passwords = {
            "password123",
            "admin123",
            "user123",
            "fleet123"
        };
        
        logger.info("=== Generador de Hashes BCrypt ===");
        logger.info("\n");
        
        for (String password : passwords) {
            String hash = encoder.encode(password);
            logger.info("Contraseña: {}", password);
            logger.info("Hash BCrypt: {}", hash);
            logger.info("Verificación: {}", encoder.matches(password, hash));
            logger.info("-".repeat(80));
        }
    }
}
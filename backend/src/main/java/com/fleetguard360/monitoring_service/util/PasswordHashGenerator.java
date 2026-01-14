package com.fleetguard360.monitoring_service.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class para generar hashes BCrypt de contraseñas
 * Útil para crear datos de prueba
 */
public class PasswordHashGenerator {
    
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Contraseñas a encriptar
        String[] passwords = {
            "password123",
            "admin123",
            "user123",
            "fleet123"
        };
        
        System.out.println("=== Generador de Hashes BCrypt ===");
        System.out.println();
        
        for (String password : passwords) {
            String hash = encoder.encode(password);
            System.out.println("Contraseña: " + password);
            System.out.println("Hash BCrypt: " + hash);
            System.out.println("Verificación: " + encoder.matches(password, hash));
            System.out.println("-".repeat(80));
        }
    }
}
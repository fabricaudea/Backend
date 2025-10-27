package com.fleetguard360.monitoring_service.service;

import com.fleetguard360.monitoring_service.model.User;
import com.fleetguard360.monitoring_service.model.LoginHistory;
import com.fleetguard360.monitoring_service.repository.UserRepository;
import com.fleetguard360.monitoring_service.repository.LoginHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCK_TIME_DURATION = 15; // minutes

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LoginHistoryRepository loginHistoryRepository;

		public String prepareUsername(String username, String context) {
			if (username == null) {
				return "desconocido";
			}
			username = username.replaceAll("[\n\r]", "_");
			logger.info("{} - Intento de login para usuario: {}", context, username);
			return username;
		}


    public void recordFailedAttempt(String username, String ipAddress) {
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            
            if (user != null) {
                int newFailAttempts = user.getFailedAttempts() + 1;
                user.setFailedAttempts(newFailAttempts);
                
                logger.warn("Usuario {} falló intento de login #{} desde IP {}", username, newFailAttempts, ipAddress);
                
                if (newFailAttempts >= MAX_FAILED_ATTEMPTS) {
                    user.setLockTime(LocalDateTime.now());
                    logger.error("Usuario {} BLOQUEADO tras {} intentos fallidos desde IP {}", 
                            username, newFailAttempts, ipAddress);
                }
                
                userRepository.save(user);
            }
            
            // Registrar intento fallido en historial
            recordLoginAttempt(user, ipAddress, false);
            
        } catch (Exception e) {
            logger.error("Error al registrar intento fallido para usuario {}: {}", username, e.getMessage());
        }
    }

    public void recordSuccessfulLogin(String username, String ipAddress) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
            
            // Resetear intentos fallidos en login exitoso
            if (user.getFailedAttempts() > 0) {
                user.setFailedAttempts(0);
                user.setLockTime(null);
                userRepository.save(user);
            }
            
            logger.info("Login exitoso para usuario {} desde IP {}", username, ipAddress);
            
            // Registrar login exitoso en historial
            recordLoginAttempt(user, ipAddress, true);
            
        } catch (Exception e) {
            logger.error("Error al registrar login exitoso para usuario {}: {}", username, e.getMessage());
        }
    }

    private void recordLoginAttempt(User user, String ipAddress, boolean success) {
        try {
            LoginHistory loginHistory = new LoginHistory();
            loginHistory.setUser(user);
            loginHistory.setLoginTime(LocalDateTime.now());
            loginHistory.setIpAddress(ipAddress);
            loginHistory.setSuccess(success);
            
            loginHistoryRepository.save(loginHistory);
            
        } catch (Exception e) {
            logger.error("Error al guardar historial de login: {}", e.getMessage());
        }
    }

    public boolean isUserLocked(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getLockTime() != null && 
                            LocalDateTime.now().isBefore(user.getLockTime().plusMinutes(LOCK_TIME_DURATION)))
                .orElse(false);
    }
    
    /**
     * Alias para isUserLocked para consistencia con el controlador
     */
    public boolean isAccountLocked(String username) {
        return isUserLocked(username);
    }
    
    /**
     * Resetea los intentos fallidos de un usuario
     */
    public void resetFailedAttempts(String username) {
        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null) {
                user.setFailedAttempts(0);
                user.setLockTime(null);
                userRepository.save(user);
								if (username != null){
									username = username.replaceAll("[\n\r]", "_");
								}
                logger.info("Intentos fallidos reseteados para usuario: {}", username);
            }
        } catch (Exception e) {
            logger.error("Error al resetear intentos fallidos para usuario {}: {}", username, e.getMessage());
        }
    }
}

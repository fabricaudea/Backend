package com.fleetguard360.monitoring_service.config;

import com.fleetguard360.monitoring_service.service.AuthenticationService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuthenticationEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationEventListener.class);

		private static final String UNKNOWN = "unknown";

    private AuthenticationService authenticationService;

		@Autowired
		public AuthenticationEventListener( AuthenticationService authenticationService) {
			this.authenticationService = authenticationService;
		}

    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        String ipAddress = getClientIP();
        
        logger.info("Evento de autenticación exitosa para usuario: {} desde IP: {}", username, ipAddress);
        authenticationService.recordSuccessfulLogin(username, ipAddress);
    }

    @EventListener
    public void handleAuthenticationFailure(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();
        String ipAddress = getClientIP();
        
        logger.warn("Evento de autenticación fallida para usuario: {} desde IP: {}", username, ipAddress);
        authenticationService.recordFailedAttempt(username, ipAddress);
    }

    private String getClientIP() {
        try {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty() && !UNKNOWN.equalsIgnoreCase(xForwardedFor)) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            String xRealIP = request.getHeader("X-Real-IP");
            if (xRealIP != null && !xRealIP.isEmpty() && !UNKNOWN.equalsIgnoreCase(xRealIP)) {
                return xRealIP;
            }
            
            return request.getRemoteAddr();
        } catch (Exception e) {
            logger.error("Error obteniendo IP del cliente: {}", e.getMessage());
            return UNKNOWN;
        }
    }
}
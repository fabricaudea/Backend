package com.fleetguard360.monitoring_service.service;

import com.fleetguard360.monitoring_service.model.User;
import com.fleetguard360.monitoring_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

		@Autowired
		public CustomUserDetailsService (UserRepository userRepository) {
			this.userRepository = userRepository;
		}

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        // Verificar si el usuario está bloqueado
        if (isUserLocked(user)) {
            throw new UsernameNotFoundException("Usuario bloqueado temporalmente debido a múltiples intentos fallidos");
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                !isUserLocked(user), // accountNonLocked
                getAuthorities(user)
        );
    }

    private boolean isUserLocked(User user) {
        if (user.getLockTime() == null) {
            return false;
        }
        
        // Verificar si han pasado 15 minutos desde el bloqueo
        LocalDateTime lockTime = user.getLockTime();
        LocalDateTime unlockTime = lockTime.plusMinutes(15);
        
        if (LocalDateTime.now().isAfter(unlockTime)) {
            // Desbloquear usuario automáticamente
            user.setFailedAttempts(0);
            user.setLockTime(null);
            userRepository.save(user);
            return false;
        }
        
        return true;
    }

    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .toList();
    }
    
    /**
     * Carga la entidad User completa por username
     * Útil para obtener información adicional del usuario después de la autenticación
     */
    public User loadUserEntityByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
    }
}
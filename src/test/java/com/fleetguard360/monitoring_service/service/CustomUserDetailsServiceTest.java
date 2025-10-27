package com.fleetguard360.monitoring_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.fleetguard360.monitoring_service.model.Role;
import com.fleetguard360.monitoring_service.model.User;
import com.fleetguard360.monitoring_service.repository.UserRepository;

class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        Role roleUser = new Role();
        roleUser.setName("USER");

        user = new User();
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setEnabled(true);
        user.setRoles(Set.of(roleUser));
    }

    @Test
    void loadUserByUsername_UserExists_ReturnsUserDetails() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertEquals("testuser", userDetails.getUsername());
        assertEquals("password123", userDetails.getPassword());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("missingUser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, 
            () -> customUserDetailsService.loadUserByUsername("missingUser"));
    }

    @Test
    void loadUserByUsername_UserLocked_ThrowsException() {
        user.setLockTime(LocalDateTime.now());
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThrows(UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserByUsername("testuser"));
    }

    @Test
    void loadUserByUsername_UserPreviouslyLocked_ButNowUnlocked_SuccessfullyLoads() {
        // Bloqueado hace más de 15 minutos
        user.setLockTime(LocalDateTime.now().minusMinutes(16));
        user.setFailedAttempts(3);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("testuser");

        assertNotNull(userDetails);
        assertTrue(userDetails.isAccountNonLocked());
        verify(userRepository).save(user);
        assertNull(user.getLockTime());
        assertEquals(0, user.getFailedAttempts());
    }

    @Test
    void loadUserEntityByUsername_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        User result = customUserDetailsService.loadUserEntityByUsername("testuser");

        assertEquals("testuser", result.getUsername());
    }

    @Test
    void loadUserEntityByUsername_NotFound_ThrowsException() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
            () -> customUserDetailsService.loadUserEntityByUsername("ghost"));
    }
}

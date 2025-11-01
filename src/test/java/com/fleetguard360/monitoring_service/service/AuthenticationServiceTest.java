package com.fleetguard360.monitoring_service.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fleetguard360.monitoring_service.model.LoginHistory;
import com.fleetguard360.monitoring_service.model.User;
import com.fleetguard360.monitoring_service.repository.LoginHistoryRepository;
import com.fleetguard360.monitoring_service.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoginHistoryRepository loginHistoryRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("admin");
        user.setFailedAttempts(0);
        user.setLockTime(null);
    }

    // ------------------------------------------------------------
    // TEST: prepareUsername
    // ------------------------------------------------------------
    @Test
    void prepareUsername_ShouldReplaceNewlinesAndReturnSanitizedValue() {
        String result = authenticationService.prepareUsername("ricardo\nmedina", "contexto");
        assertEquals("ricardo_medina", result);
    }

    @Test
    void prepareUsername_ShouldReturnDesconocido_WhenNull() {
        String result = authenticationService.prepareUsername(null, "contexto");
        assertEquals("desconocido", result);
    }

    // ------------------------------------------------------------
    // TEST: recordFailedAttempt
    // ------------------------------------------------------------
    @Test
    void recordFailedAttempt_ShouldIncreaseFailedAttempts() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        authenticationService.recordFailedAttempt("admin", "127.0.0.1");

        assertEquals(1, user.getFailedAttempts());
        verify(userRepository).save(user);
        verify(loginHistoryRepository).save(any(LoginHistory.class));
    }

    @Test
    void recordFailedAttempt_ShouldLockUserAfterMaxAttempts() {
        user.setFailedAttempts(2);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        authenticationService.recordFailedAttempt("admin", "127.0.0.1");

        assertNotNull(user.getLockTime());
        assertEquals(3, user.getFailedAttempts());
        verify(userRepository).save(user);
    }

    @Test
    void recordFailedAttempt_ShouldHandleUserNotFoundGracefully() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertDoesNotThrow(() ->
                authenticationService.recordFailedAttempt("unknown", "127.0.0.1")
        );

        verify(loginHistoryRepository).save(any(LoginHistory.class));
    }

    // ------------------------------------------------------------
    // TEST: recordSuccessfulLogin
    // ------------------------------------------------------------
    @Test
    void recordSuccessfulLogin_ShouldResetFailedAttempts() {
        user.setFailedAttempts(2);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        authenticationService.recordSuccessfulLogin("admin", "127.0.0.1");

        assertEquals(0, user.getFailedAttempts());
        assertNull(user.getLockTime());
        verify(userRepository, atLeastOnce()).save(user);
        verify(loginHistoryRepository).save(any(LoginHistory.class));
    }

    @Test
    void recordSuccessfulLogin_ShouldThrowWhenUserNotFound() {
        when(userRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(() ->
                authenticationService.recordSuccessfulLogin("unknown", "127.0.0.1")
        );
    }

    // ------------------------------------------------------------
    // TEST: isUserLocked / isAccountLocked
    // ------------------------------------------------------------
    @Test
    void isUserLocked_ShouldReturnTrueIfLockNotExpired() {
        user.setLockTime(LocalDateTime.now().minusMinutes(5));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        assertTrue(authenticationService.isUserLocked("admin"));
        assertTrue(authenticationService.isAccountLocked("admin"));
    }

    @Test
    void isUserLocked_ShouldReturnFalseIfLockExpired() {
        user.setLockTime(LocalDateTime.now().minusMinutes(20));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        assertFalse(authenticationService.isUserLocked("admin"));
    }

    @Test
    void isUserLocked_ShouldReturnFalseIfUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertFalse(authenticationService.isUserLocked("unknown"));
    }

    // ------------------------------------------------------------
    // TEST: resetFailedAttempts
    // ------------------------------------------------------------
    @Test
    void resetFailedAttempts_ShouldResetUserAttempts() {
        user.setFailedAttempts(3);
        user.setLockTime(LocalDateTime.now());
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        authenticationService.resetFailedAttempts("admin");

        assertEquals(0, user.getFailedAttempts());
        assertNull(user.getLockTime());
        verify(userRepository).save(user);
    }

    @Test
    void resetFailedAttempts_ShouldDoNothingIfUserNotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        authenticationService.resetFailedAttempts("unknown");
        verify(userRepository, never()).save(any());
    }
}

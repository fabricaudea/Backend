package com.fleetguard360.monitoring_service.config;

import com.fleetguard360.monitoring_service.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration

@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("!demo")

public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("*"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable()) // Para APIs REST, deshabilitar CSRF
            .authorizeHttpRequests(authz -> authz
                // Endpoints públicos - no requieren autenticación
                .requestMatchers("/api/auth/login", "/api/frontend/auth/login", 
                               "/api/health/**", "/api/test/**").permitAll()
                // Endpoints específicos del frontend
                .requestMatchers("/api/frontend/auth/me").authenticated()
                .requestMatchers("/api/frontend/auth/logout").authenticated()
                .requestMatchers("/api/frontend/vehicles/**").hasAnyRole("ADMIN", "USER")
                // Endpoints originales de vehículos - requieren autenticación con roles específicos
                .requestMatchers("/api/vehicles/**").hasAnyRole("ADMIN", "USER")
                // Endpoints que requieren roles específicos
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN")
                // Endpoint de logout requiere autenticación
                .requestMatchers("/api/auth/logout", "/api/auth/status").authenticated()
                // Todos los demás requieren autenticación
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .maximumSessions(1) // Máximo una sesión por usuario
                .maxSessionsPreventsLogin(false) // Permitir nueva sesión, expulsar la anterior
            );

        return http.build();
    }
}
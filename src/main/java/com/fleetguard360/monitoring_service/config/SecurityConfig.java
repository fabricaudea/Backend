package com.fleetguard360.monitoring_service.config;

import com.fleetguard360.monitoring_service.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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
import org.springframework.security.config.http.SessionCreationPolicy;


import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private CustomUserDetailsService userDetailsService;

		private static final String ADMIN = "ADMIN";

		private static final String USER = "USER";

		@Autowired
		public SecurityConfig(CustomUserDetailsService userDetailsService) {
			this.userDetailsService = userDetailsService;
		}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:3001"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(Arrays.asList("*"));
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Endpoints públicos, SIEMPRE primero
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/frontend/auth/login",
                                "/api/health/**",
                                "/api/test/**",
                                "/index.html",
                                "/map/**",
                                "/css/**",
                                "/js/**",
                                "/images/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**"
                        ).permitAll()
                        // Endpoints autenticados y con roles
                        .requestMatchers("/api/frontend/auth/me").authenticated()
                        .requestMatchers("/api/frontend/auth/logout").authenticated()
                        .requestMatchers("/api/frontend/vehicles/**").hasAnyRole(ADMIN, USER)
                        .requestMatchers("/api/vehicles/**").hasAnyRole(ADMIN, USER)
                        .requestMatchers("/api/admin/**").hasRole(ADMIN)
                        .requestMatchers("/api/user/**").hasAnyRole(USER, ADMIN)
                        .requestMatchers("/api/auth/logout", "/api/auth/status").authenticated()
                        .anyRequest().authenticated()
                )
                // EXPLÍCITO: Desactiva Basic y Form Login. Solo tokens/JWT
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(formLogin -> formLogin.disable());
        // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // cuando actives tu filtro JWT

        return http.build();
    }


    /*
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // ✅ AGREGADO: Sesiones stateless para JWT
                )
                .authorizeHttpRequests(authz -> authz
                        // Endpoints públicos - no requieren autenticación
                        .requestMatchers("/api/auth/login", "/api/frontend/auth/login",
                                "/api/health/**", "/api/test/**",
                                "/index.html", "/map/**", "/css/**", "/js/**", "/images/**",
                                "/swagger-ui/**", "/v3/api-docs/**").permitAll() // ✅ CORREGIDO: /swagger-ui/**
                        // Endpoints específicos del frontend
                        .requestMatchers("/api/frontend/auth/me").authenticated()
                        .requestMatchers("/api/frontend/auth/logout").authenticated()
                        .requestMatchers("/api/frontend/vehicles/**").hasAnyRole(ADMIN, USER)
                        // Endpoints originales de vehículos
                        .requestMatchers("/api/vehicles/**").hasAnyRole(ADMIN, USER)
                        // Endpoints que requieren roles específicos
                        .requestMatchers("/api/admin/**").hasRole(ADMIN)
                        .requestMatchers("/api/user/**").hasAnyRole(USER, ADMIN)
                        // Endpoint de logout requiere autenticación
                        .requestMatchers("/api/auth/logout", "/api/auth/status").authenticated()
                        // Todos los demás requieren autenticación
                        .anyRequest().authenticated()
                );
        // ✅ AGREGADO: Filtro JWT (veremos si lo tienes o lo creamos)
        // .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
*/

}
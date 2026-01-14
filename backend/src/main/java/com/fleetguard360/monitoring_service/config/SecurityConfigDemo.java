package com.fleetguard360.monitoring_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import org.springframework.security.crypto.password.NoOpPasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
@Profile("demo")
public class SecurityConfigDemo {

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails demo = User.withUsername("demo")
                .password("{noop}demo") // usuario/clave de demo
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(demo);
    }

    @Bean @SuppressWarnings("deprecation")
    public NoOpPasswordEncoder passwordEncoder() {
        return (NoOpPasswordEncoder) NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(
            UserDetailsService uds, NoOpPasswordEncoder enc) {
        DaoAuthenticationProvider p = new DaoAuthenticationProvider();
        p.setUserDetailsService(uds);
        p.setPasswordEncoder(enc);
        return p;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable());
        http.authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/api/health/**", "/api/auth/**").permitAll()
                .anyRequest().permitAll() // en demo dejamos todo abierto
        );
        http.httpBasic(Customizer.withDefaults());
        http.authenticationProvider(authenticationProvider(userDetailsService(), passwordEncoder()));
        return http.build();
    }
}

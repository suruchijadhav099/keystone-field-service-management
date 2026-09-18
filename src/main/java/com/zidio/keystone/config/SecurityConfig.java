package com.zidio.keystone.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtFilter jwtFilter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                // Use the existing CorsConfig.java
                .cors(Customizer.withDefaults())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Public endpoints
                        .requestMatchers(
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()

                        // Manager only
                        .requestMatchers(
                                "/api/dashboard/**",
                                "/api/users/**"
                        ).hasRole("MANAGER")

                        // Work orders
                        .requestMatchers(
                                "/api/work-orders/**"
                        ).authenticated()

                        // Part usage
                        .requestMatchers(
                                "/api/part-usage/**"
                        ).hasAnyRole(
                                "MANAGER",
                                "TECHNICIAN"
                        )

                        // Time logs
                        .requestMatchers(
                                "/api/time-logs/**"
                        ).hasAnyRole(
                                "MANAGER",
                                "TECHNICIAN"
                        )

                        // Customers and sites
                        .requestMatchers(
                                "/api/customers/**",
                                "/api/sites/**"
                        ).hasAnyRole(
                                "MANAGER",
                                "DISPATCHER",
                                "TECHNICIAN",
                                "CUSTOMER"
                        )

                        // Legacy jobs endpoints
                        .requestMatchers(
                                "/api/jobs/**"
                        ).hasAnyRole(
                                "MANAGER",
                                "DISPATCHER",
                                "TECHNICIAN",
                                "CUSTOMER"
                        )

                        // Everything else requires login
                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
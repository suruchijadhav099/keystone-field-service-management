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

                .cors(cors -> cors.configurationSource(request -> {
                    org.springframework.web.cors.CorsConfiguration config =
                            new org.springframework.web.cors.CorsConfiguration();

                    config.setAllowedOrigins(java.util.List.of(
                            "http://localhost:5173",
                            "http://127.0.0.1:5173"
                    ));

                    config.setAllowedMethods(java.util.List.of(
                            "GET",
                            "POST",
                            "PUT",
                            "DELETE",
                            "PATCH",
                            "OPTIONS"
                    ));

                    config.setAllowedHeaders(java.util.List.of("*"));
                    config.setAllowCredentials(true);

                    return config;
                }))

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/api/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()

                        .requestMatchers(
                                "/api/dashboard/**",
                                "/api/users/**"
                        ).hasRole("MANAGER")

                        .requestMatchers(
                                "/api/work-orders/**"
                        ).authenticated()
                        .requestMatchers("/api/part-usage/**").hasAnyRole("MANAGER","TECHNICIAN")
                        .requestMatchers(
                                "/api/time-logs/**"
                        ).hasAnyRole(
                                "MANAGER",
                                "TECHNICIAN"
                        )

                        .requestMatchers(
                                "/api/customers/**",
                                "/api/sites/**"
                        ).hasAnyRole(
                                "MANAGER",
                                "DISPATCHER",
                                "TECHNICIAN",
                                "CUSTOMER"
                        )

                        .requestMatchers(
                                "/api/jobs/**"
                        ).hasAnyRole(
                                "MANAGER",
                                "DISPATCHER",
                                "TECHNICIAN",
                                "CUSTOMER"
                        )

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}
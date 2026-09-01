package com.findmytutor.findmytutor_backend.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.findmytutor.findmytutor_backend.security.JwtAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .cors(cors -> {})

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Public authentication APIs
                        .requestMatchers("/api/auth/**").permitAll()

                        // Parent can search nearby tutors
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/tutors/nearby"
                        ).hasRole("PARENT")

                        // Tutor profile APIs
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/tutors/profile"
                        ).hasRole("TUTOR")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/tutors/profile"
                        ).hasRole("TUTOR")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/tutors/profile"
                        ).hasRole("TUTOR")
                        


                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

// Parent request APIs
.requestMatchers(
        HttpMethod.POST,
        "/api/requests/tutor/**"
).hasRole("PARENT")

.requestMatchers(
        HttpMethod.GET,
        "/api/requests/parent"
).hasRole("PARENT")

// Tutor request APIs
.requestMatchers(
        HttpMethod.GET,
        "/api/requests/tutor"
).hasRole("TUTOR")

.requestMatchers(
        HttpMethod.POST,
        "/api/requests/*/accept"
).hasRole("TUTOR")

.requestMatchers(
        HttpMethod.POST,
        "/api/requests/*/reject"
).hasRole("TUTOR")

.anyRequest().authenticated()

                        // Everything else requires authentication
                        // .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    configuration.setAllowedOrigins(List.of(
            "https://fmt-frontend-b7oj569rp-artizo2.vercel.app"
    ));

    configuration.setAllowedMethods(List.of(
            "GET",
            "POST",
            "PUT",
            "DELETE",
            "OPTIONS"
    ));

    configuration.setAllowedHeaders(List.of("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration("/**", configuration);

    return source;
}
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
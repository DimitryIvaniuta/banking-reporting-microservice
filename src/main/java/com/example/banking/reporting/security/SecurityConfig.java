package com.example.banking.reporting.security;

import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration for API authorization.
 */
@Configuration
@EnableMethodSecurity(jsr250Enabled = true)
@EnableConfigurationProperties(SecurityUsersProperties.class)
public class SecurityConfig {

    /**
     * Configures stateless HTTP Basic authentication with role-based authorization.
     *
     * @param http spring security builder
     * @return built filter chain
     * @throws Exception when the chain cannot be configured
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/system/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/api/reports/**").authenticated()
                .anyRequest().authenticated());
        return http.build();
    }

    /**
     * Creates local in-memory users from configuration for development/test.
     *
     * @param properties configured users and roles
     * @param passwordEncoder password encoder for secure in-memory storage
     * @return user details service
     */
    @Bean
    public UserDetailsService userDetailsService(SecurityUsersProperties properties, PasswordEncoder passwordEncoder) {
        List<UserDetails> users = properties.getUsers().stream()
            .map(u -> User.withUsername(u.getUsername())
                .password(passwordEncoder.encode(u.getPassword()))
                .roles(u.getRoles().toArray(String[]::new))
                .build())
            .toList();
        return new InMemoryUserDetailsManager(users);
    }

    /**
     * Password encoder used for local in-memory credentials.
     *
     * @return bcrypt encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
